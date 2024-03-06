@file:OptIn(ExperimentalStdlibApi::class)

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManagerEvent
import no.nordicsemi.kotlin.mesh.core.layers.NetworkParameters
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.HeartbeatMessage
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.UpperTransportPdu
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.Logger
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.schedule
import kotlin.math.min
import kotlin.time.Duration

private sealed class Message {

    data class LowerTransportLayerPdu(val message: LowerTransportPdu) : Message()

    data class Acknowledgement(val ack: SegmentAcknowledgementMessage) : Message()

    data object None : Message()
}

private sealed class SecurityError : Exception() {

    /**
     * Thrown internally when a possible replay attack is detected. This error is not propagated to
     * higher levels. When it is caught, te received packet discarded.
     */
    data object ReplayAttack : SecurityError()

}

internal class LowerTransportLayer(private val networkManager: NetworkManager) {
    private val network = networkManager.meshNetwork
    private val logger: Logger?
        get() = networkManager.logger
    private val storage = networkManager.securePropertiesStorage
    val scope = networkManager.scope
    private val mutex = Mutex()
    private val networkParams: NetworkParameters
        get() = networkManager.networkParameters

    private val incompleteSegments = mutableMapOf<UInt, MutableList<SegmentedMessage?>>()
    private val acknowledgements = mutableMapOf<Address, SegmentAcknowledgementMessage>()
    private val discardTimers = mutableMapOf<UInt, Timer>()
    private val acknowledgementTimers = mutableMapOf<UInt, Timer>()
    private val outgoingSegments =
        mutableMapOf<UShort, OutgoingSegment>()
    private val unicastRetransmissionsTimers = mutableMapOf<UShort, Timer>()
    private val remainingNumberOfUnicastRetransmissions =
        mutableMapOf<UShort, RemainingNumberOfUnicastRetransmissions>()
    private var multicastRetransmissionsTimers = mutableMapOf<UShort, Timer>()
    private var remainingNumberOfMulticastRetransmissions = mutableMapOf<UShort, UByte>()
    private val segmentTtl = mutableMapOf<UShort, UByte>()

    /**
     * This method handles the received Network PDU. If needed, it will reassemble the messages,
     * send block acknowledgement to the sender, and pass the Upper Transport PDU to the Upper
     * Transport Layer.
     *
     * @param networkPdu Network PDU received.
     */
    suspend fun handle(networkPdu: NetworkPdu): MeshMessage? {
        require(networkPdu.transportPdu.size > 1) { return null }

        mutex.withLock {
            require(checkAgainstReplayAttack(networkPdu)) { throw SecurityError.ReplayAttack }
            val segmented = networkPdu.isSegmented
            val msg = if (segmented) {
                when (networkPdu.type) {
                    LowerTransportPduType.ACCESS_MESSAGE -> {
                        SegmentedAccessMessage.init(networkPdu)?.let {
                            logger?.d(LogCategory.LOWER_TRANSPORT) {
                                "$it received (decrypted using key: ${it.networkKey})"
                            }
                            assemble(it, networkPdu)?.let { pdu ->
                                Message.LowerTransportLayerPdu(pdu)
                            }
                        }
                    }

                    LowerTransportPduType.CONTROL_MESSAGE -> {
                        SegmentedControlMessage.init(networkPdu)?.let {
                            logger?.d(LogCategory.LOWER_TRANSPORT) {
                                "$it received (decrypted using key: ${it.networkKey})"
                            }
                            assemble(it, networkPdu)?.let { pdu ->
                                Message.LowerTransportLayerPdu(pdu)
                            }
                        }
                    }
                }
            } else {
                when (networkPdu.type) {
                    LowerTransportPduType.ACCESS_MESSAGE -> AccessMessage.init(networkPdu)?.let {
                        logger?.d(LogCategory.LOWER_TRANSPORT) {
                            "$it received (decrypted using key: ${it.networkKey})"
                        }
                        Message.LowerTransportLayerPdu(it)
                    }

                    LowerTransportPduType.CONTROL_MESSAGE -> {
                        val opCode = (networkPdu.transportPdu[0].toUByte().toInt() and 0x7F)
                        if (opCode == 0x00) {
                            SegmentAcknowledgementMessage.init(networkPdu)?.let {
                                logger?.d(LogCategory.LOWER_TRANSPORT) {
                                    "$it received (decrypted using key: ${it.networkKey})"
                                }
                                Message.Acknowledgement(it)
                            }
                        } else {
                            ControlMessage.init(networkPdu)?.let {
                                logger?.d(LogCategory.LOWER_TRANSPORT) {
                                    "$it received (decrypted using key: ${it.networkKey})"
                                }
                                Message.LowerTransportLayerPdu(it)
                            }
                        }
                    }
                }
            }
            return try {
                msg?.let {
                    when (it) {
                        is Message.LowerTransportLayerPdu -> {
                            networkManager.upperTransportLayer.handle(it.message)
                        }

                        is Message.Acknowledgement -> {
                            handle(ack = it.ack)
                            null
                        }

                        is Message.None -> {
                            // Ignore
                            null
                        }
                    }
                }
            } catch (ex: Exception) {
                // TODO
                null
            }
        }
    }

    /**
     * Sends the given Upper Transport Message.
     *
     * @param pdu             Upper Transport PDU to be sent.
     * @param initialTtl      TTL to be used to send the message.
     * @param networkKey      Network key to be used to encrypt the message.
     */
    suspend fun sendUnsegmentedUpperTransportPdu(
        pdu: UpperTransportPdu,
        initialTtl: UByte?,
        networkKey: NetworkKey
    ) {
        network.localProvisioner?.node?.let { node ->
            val ttl = initialTtl ?: node.defaultTTL ?: networkManager.networkParameters.defaultTtl
            val message = AccessMessage(pdu = pdu, networkKey = networkKey)
            try {
                logger?.i(LogCategory.LOWER_TRANSPORT) { "Sending $message" }
                networkManager.networkLayer.send(
                    pdu = message,
                    type = PduType.NETWORK_PDU,
                    ttl = ttl
                )
            } catch (ex: Exception) {
                logger?.e(LogCategory.LOWER_TRANSPORT) { "$ex" }
                pdu.takeIf {
                    it.message != null && it.message.isAcknowledged
                }?.let {
                    // TODO
                }
            }
        }
    }

    /**
     * Sends the given Upper Transport Message.
     *
     * @param pdu          Upper Transport PDU to be sent.
     * @param initialTtl   TTL to be used to send the message.
     * @param networkKey   Network key to be used to encrypt the message.
     */
    suspend fun sendSegmentedUpperTransportPdu(
        pdu: UpperTransportPdu,
        initialTtl: UByte?,
        networkKey: NetworkKey
    ) {
        val provisionerNode = network.localProvisioner?.node ?: return
        // Last 13 bits of the sequence number are known as seqZero.
        val sequenceZero = (pdu.sequence and 0x1FFFu).toUShort()
        // Number of segments to be sent.
        val count = (pdu.transportPdu.size + 11) / 12

        // Create all segments to be sent.
        outgoingSegments[sequenceZero] = OutgoingSegment(
            destination = pdu.destination,
            segments = MutableList(size = count, init = { null })
        )
        for (index in 0 until count) {
            outgoingSegments[sequenceZero]!!.segments[index] = SegmentedAccessMessage.init(
                pdu = pdu, networkKey = networkKey, offset = index.toUByte()
            )
        }
        // Store the TTL with which the segments are to be sent.
        segmentTtl[sequenceZero] = initialTtl ?: provisionerNode.defaultTTL
                ?: networkManager.networkParameters.defaultTtl
        // Initialize the retransmission counters.
        if (pdu.destination is UnicastAddress) {
            remainingNumberOfUnicastRetransmissions[sequenceZero] =
                RemainingNumberOfUnicastRetransmissions(
                    total = networkManager.networkParameters.sarUnicastRetransmissionsCount,
                    withoutProgress = networkManager.networkParameters.sarMulticastRetransmissionsCount
                )
        } else {
            remainingNumberOfMulticastRetransmissions[sequenceZero] =
                networkManager.networkParameters.sarMulticastRetransmissionsCount
        }
        sendSegments(sequenceZero)
    }

    /**
     * Sends a Heartbeat message.
     *
     * @param heartbeat   Heartbeat message to be sent.
     * @param networkKey         Network key to be used to encrypt the message.
     */
    suspend fun send(heartbeat: HeartbeatMessage, networkKey: NetworkKey) {
        val message = ControlMessage(heartbeatMessage = heartbeat, networkKey = networkKey)
        try {
            logger?.i(LogCategory.LOWER_TRANSPORT) { "Sending $message" }
            networkManager.networkLayer.send(
                pdu = message,
                type = PduType.NETWORK_PDU,
                ttl = heartbeat.initialTtl
            )
        } catch (ex: Exception) {
            logger?.e(LogCategory.LOWER_TRANSPORT) { "$ex" }
        }
    }

    /**
     * Returns whether the Lower Transport Layer is in progress of receiving a segmented message
     * from the given address.
     *
     * @param address Address of the sender.
     * @return true if some, but not all packets ofr a segmented message were received from the
     *         given source address or false if no packets were received or the message was complete
     *         before calling this method.
     */
    fun isReceivingMessage(address: Address): Boolean = incompleteSegments.any {
        ((it.key shr 16) and 0xFFFFu) == address.toUInt()
    }

    private suspend fun checkAgainstReplayAttack(networkPdu: NetworkPdu): Boolean {
        require(
            networkPdu.destination !is UnicastAddress ||
                    network.localProvisioner?.node?.containsElementWithAddress(
                        address = networkPdu.destination
                    ) ?: false
        ) { return true }

        val sequence = networkPdu.messageSequence
        val receivedSeqAuth = (networkPdu.ivIndex.toULong() shl 24) or sequence.toULong()
        val source = networkPdu.source as UnicastAddress
        storage.lastSeqAuthValue(network.uuid, source)?.let { localSeqAuth ->
            // In general, the SeqAuth of the received message must be greater than SeqAuth of any
            // previously received message from the same source. However, for SAR (Segmentation and
            // Reassembly) sessions, the SeqAuth of the message must be checked not the SeqAuth of
            // the segment. If SAR is active (at last one segment for the same SeqAuth has been
            // previously received), the segments ma be processed in an order. The SeqAuth of this
            // message must be greater or equal to the last one.

            var reassemblyInProgress = false
            if (networkPdu.isSegmented) {
                val sequenceZero = (sequence and 0x1FFFu).toUShort()
                val key = (networkPdu.source.address.toUInt() shl 16) or
                        (sequenceZero and 0x1FFFu).toUInt()
                reassemblyInProgress = (incompleteSegments[key] != null) ||
                        (acknowledgements[networkPdu.source.address]?.sequenceZero == sequenceZero)
            }

            // As the messages are processed in a concurrent queue, it may happen that two messages
            // sent almost immediately were received in the right order, but are processed in the
            // opposite order. To handle that case, the previous SeqAuth is stored. If the received
            // message has SeqAuth less than the last one, but greater thant he previous one, it
            // could not be used to reply attack, as no message with that SeqAuth was ever reached.
            var missed = false
            storage.previousSeqAuthValue(network.uuid, source)
                ?.let { previousSeqAuth ->
                    missed = (receivedSeqAuth < localSeqAuth) && (receivedSeqAuth > previousSeqAuth)
                }

            // Validate
            require((receivedSeqAuth > localSeqAuth) || missed || reassemblyInProgress) {
                // Ignore that message.
                logger?.w(LogCategory.LOWER_TRANSPORT) {
                    "Discarding packet(seqAuth: $receivedSeqAuth, expected > $localSeqAuth)."
                }
                return false
            }

            // The message is valid. Remember the previous SeqAuth.
            val newPreviousSeqAuth = min(receivedSeqAuth, localSeqAuth)
            storage.storePreviousSeqAuthValue(
                uuid = network.uuid,
                source = networkPdu.source,
                seqAuth = newPreviousSeqAuth
            )

            // If the message was processed after its successor, don;t overwrite the last SeqAuth
            if (missed) return true
        }

        storage.storeLastSeqAuthValue(
            uuid = network.uuid,
            source = source,
            lastSeqAuth = receivedSeqAuth
        )
        return true
    }

    private suspend fun assemble(
        segment: SegmentedMessage,
        networkPdu: NetworkPdu
    ): LowerTransportPdu? {
        val key = (networkPdu.source.address.toUInt() shl 16) or
                (segment.sequenceZero and 0x1FFFu).toUInt()

        // If the received segment comes from an already completed and acknowledged message, send
        // the same ACK immediately.
        acknowledgements[segment.source.address]?.takeIf { lastAck ->
            lastAck.sequenceZero == segment.sequenceZero
        }?.let { lastAck ->
            network.localProvisioner?.node?.let { provisionerNode ->
                require(acknowledgementTimers[key] == null) {
                    logger?.d(LogCategory.LOWER_TRANSPORT) {
                        "Message already acknowledged, ACK sent recently."
                    }
                    return null
                }
                val timer = Timer()
                acknowledgementTimers[key] = timer
                timer.schedule(
                    delay = networkParams.completeAcknowledgementTimerInterval.inWholeMilliseconds
                ) {
                    acknowledgementTimers.remove(key)?.let {
                        it.cancel()
                        it.purge()
                    }
                }
                logger?.d(LogCategory.LOWER_TRANSPORT) {
                    "Message already acknowledged, sending ACK immediately."
                }
                val ttl = if (networkPdu.ttl > 0u) {
                    provisionerNode.defaultTTL ?: networkManager.networkParameters.defaultTtl
                } else 0u
                sendAck(lastAck, ttl)
            } ?: run {
                acknowledgements.remove(segment.source.address)
            }
            return null
        }

        // Remove the last ACK. The source Node has sent a new message, so the last ACK must have
        // been received.
        acknowledgements.remove(segment.source.address)
        if (segment.isSingleSegment) {
            val segments = listOf(segment)
            val message = segments.reassembled()
            logger?.i(LogCategory.LOWER_TRANSPORT) { "$message received." }
            // A single segment message may immediately be acknowledged.
            network.localProvisioner?.node?.takeIf {
                it.containsElementWithAddress(networkPdu.destination)
            }?.let { provisionerNode ->
                val ttl = if (networkPdu.ttl > 0u) {
                    provisionerNode.defaultTTL ?: networkManager.networkParameters.defaultTtl
                } else 0u
                sendAck(segments, ttl)
            }
            return message
        } else {
            // If a message is composed of multiple segments, they all need to be received before it
            // can be processed.
            if (incompleteSegments[key] == null) {
                incompleteSegments[key] = MutableList(segment.count) { null }
            }

            require(incompleteSegments[key]!!.size > segment.index) {
                // Segment is invalid. We can stop here.
                logger?.w(LogCategory.LOWER_TRANSPORT) { "Invalid segment" }
                return null
            }
            incompleteSegments[key]!![segment.index] = segment

            // If all segments were received, send ACK and send the PDU to Upper Transport Layer for
            // processing.
            if (incompleteSegments[key]!!.isComplete()) {
                val allSegments = incompleteSegments.remove(key)!!
                val message = allSegments.reassembled()
                logger?.i(LogCategory.LOWER_TRANSPORT) { "$message received." }
                // If the access message was targeting directly the local Provisioner...
                network.localProvisioner?.node?.takeIf {
                    it.containsElementWithAddress(networkPdu.destination)
                }?.let { provisionerNode ->
                    // Invalidate timers
                    discardTimers.remove(key)?.let {
                        it.cancel()
                        it.purge()
                    }
                    acknowledgementTimers.remove(key)?.let {
                        it.cancel()
                        it.purge()
                    }

                    // ...and send the ACK that all segments were received.
                    val ttl = if (networkPdu.ttl > 0u) {
                        provisionerNode.defaultTTL ?: networkManager.networkParameters.defaultTtl
                    } else 0u
                    sendAck(allSegments, ttl)
                }
                return message
            } else {
                // The Provisioner shall send black acknowledgement only if the message was send
                // directly to it's Unicast Address.
                network.localProvisioner?.node?.takeIf {
                    it.containsElementWithAddress(networkPdu.destination)
                }?.let { provisionerNode ->
                    // If the Lower Transport Layer receives any segment while the SAR Discard Timer
                    // is active, the timer shall be restarted.
                    discardTimers[key]?.let {
                        it.cancel()
                        it.purge()
                    }
                    val discardTimer = Timer().also {
                        discardTimers[key] = it
                    }

                    discardTimer.schedule(delay = networkParams.discardTimeout.inWholeMilliseconds) {
                        incompleteSegments.remove(key)?.let { segments ->
                            var marks = 0
                            segments.forEach { segment ->
                                segment?.let {
                                    marks = marks or 1 shl it.segmentOffset.toInt()
                                }
                            }
                            logger?.w(LogCategory.LOWER_TRANSPORT) {
                                "Discard timeout expired, cancelling message (src " +
                                        "${
                                            MeshAddress.create((key shr 16).toUShort())
                                                .toHex(prefix0x = true)
                                        }, " +
                                        "seqZero ${key and 0x1FFFu}, received segments: " +
                                        marks.toHexString()
                            }
                        }
                        discardTimers.remove(key)?.also {
                            it.cancel()
                            it.purge()
                        }
                        acknowledgementTimers.remove(key)?.apply {
                            cancel()
                            purge()
                        }
                    }
                    // When a segment is received the SAR Acknowledgement timer shall be
                    // (re)started.
                    acknowledgementTimers[key]?.let {
                        it.cancel()
                        it.purge()
                    }

                    val defaultTtl = provisionerNode.defaultTTL
                        ?: networkManager.networkParameters.defaultTtl
                    val ackTimerInterval = networkManager.networkParameters
                        .acknowledgementTimerInterval(segment.lastSegmentNumber)
                    acknowledgementTimers[key] = Timer().also { ackTimer ->
                        ackTimer.schedule(delay = ackTimerInterval.inWholeMilliseconds) {
                            val segments = requireNotNull(incompleteSegments[key]) {
                                acknowledgementTimers.remove(key)
                                return@schedule
                            }
                            scope.launch {
                                // When the SAR Acknowledgement timer expires, the lower transport
                                // layer shall send a Segment Acknowledgement Message.
                                val ttl = if (networkPdu.ttl > 0u) defaultTtl else 0u
                                logger?.d(LogCategory.LOWER_TRANSPORT) {
                                    "SAR Acknowledgement timer expired, sending ACK"
                                }
                                sendAck(segments, ttl)

                                // If Segment Acknowledgment retransmission is enabled and the
                                // number of segments of the segmented message is longer than
                                // the SAR Segments Threshold, the lower transport layer should
                                // retransmit the acknowledgment specified number of times.
                                val initialCount =
                                    networkManager.networkParameters.sarAcknowledgementRetransmissionCount
                                var count = initialCount
                                if (count > 0u &&
                                    segment.lastSegmentNumber >= networkManager.networkParameters.sarSegmentsThreshold
                                ) {
                                    val interval =
                                        networkManager.networkParameters.segmentReceptionInterval
                                    acknowledgementTimers[key] = Timer().also {
                                        if (count > 1u) {
                                            it.schedule(
                                                delay = 0L,
                                                period = interval.inWholeMilliseconds
                                            ) {
                                                scope.launch {
                                                    logger?.d(LogCategory.LOWER_TRANSPORT) {
                                                        "Retransmitting ACK(${1u + initialCount - count}/$initialCount)"
                                                    }
                                                    sendAck(segments, ttl)
                                                    // Decrement the counter.
                                                    count = (count - 1u).toUByte()
                                                    if (count == 0.toUByte()) {
                                                        it.cancel()
                                                        it.purge()
                                                    }
                                                }
                                            }
                                        } else {
                                            it.schedule(delay = interval.inWholeMilliseconds) {
                                                scope.launch {
                                                    logger?.d(LogCategory.LOWER_TRANSPORT) {
                                                        "Retransmitting ACK(${1u + initialCount - count}/$initialCount)"
                                                    }
                                                    sendAck(segments, ttl)
                                                    // Decrement the counter.
                                                    count = (count - 1u).toUByte()
                                                    if (count == 0.toUByte()) {
                                                        it.cancel()
                                                        it.purge()
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                return null
            }
        }
    }

    /**
     * This method handles the Segment Acknowledgment Message.
     *
     * @param ack The Segment Acknowledgment Message received.
     */
    private suspend fun handle(ack: SegmentAcknowledgementMessage) {
        // Ensure the ACK is for some message that has been sent.
        val (destination, segments) =
            outgoingSegments[ack.sequenceZero] ?: return
        val (total, withProgress) =
            remainingNumberOfUnicastRetransmissions[ack.sequenceZero] ?: return
        require(ack.source.address == destination.address || ack.isOnBehalfOfLowePowerNode) {
            return
        }

        // Is the target Node busy?
        require(!ack.isBusy) {
            cancelTransmissionOfSegments(ack.sequenceZero, LowerTransportError.Busy)
            return
        }

        // Whether a progress has been made since the previous ACK
        var progress = false

        // Clear all the acknowledged segments
        for (index in segments.indices) {
            if (ack.isSegmentReceived(index)) {
                outgoingSegments[ack.sequenceZero]?.segments?.takeIf {
                    it[index] != null
                }?.let {
                    progress = true
                    it[index] = null
                }
            }
        }

        // If all the segments were acknowledged, notify the manager.
        if (outgoingSegments[ack.sequenceZero]?.segments?.hasMore() == false) {
            cancelTransmissionOfSegments(ack.sequenceZero, null)
        } else {
            // Check if the SAR Unicast Retransmission timer is running.
            require(unicastRetransmissionsTimers[ack.sequenceZero] != null) {
                // If not, that means that the segments are just being retransmitted
                // and we're done here. We shall receive a new acknowledgment in a bit.
                return
            }
            // Check if more retransmissions are possible.
            require(total > 0u && withProgress > 0u) {
                // If not, the running SAR Unicast Retransmissions timer will cancel
                // the message when it expires. Perhaps another acknowledgment will
                // be received before acknowledging all segments.
                return
            }
            // Stop the unicast retransmissions timer.
            unicastRetransmissionsTimers.remove(ack.sequenceZero)?.let {
                it.cancel()
                it.purge()
            }
            // Decrement the counters.
            // If a progress has been made, reset the remaining number of
            // retransmissions with progress to its initial value.
            remainingNumberOfUnicastRetransmissions[ack.sequenceZero] =
                RemainingNumberOfUnicastRetransmissions(
                    total = (total - 1u).toUByte(),
                    withoutProgress = if (progress)
                        networkManager.networkParameters.sarUnicastRetransmissionsWithoutProgressCount
                    else (withProgress - 1u).toUByte()
                )
            // Lastly, send again all packets that were not acknowledged.
            sendSegments(ack.sequenceZero)
        }
    }

    /**
     * Attempts to send the Segment Acknowledgement Message to the given address. It will attempt to
     * send if hte local provisioner is set and has the Unicast Address assigned.
     *
     * @param segments Segmented message to be acknowledged.
     * @param ttl      TTL to be used to send the ACK.
     */
    private suspend fun sendAck(segments: List<SegmentedMessage?>, ttl: UByte) {
        val ack = SegmentAcknowledgementMessage.init(segments)
        if (segments.isComplete()) acknowledgements[ack.destination.address] = ack
        sendAck(ack, ttl)
    }

    /**
     * Sends the given ACK on the global background queue.
     *
     * @param ack ACK to be sent.
     * @param ttl TTL to be used to send the ACK.
     */
    private suspend fun sendAck(ack: SegmentAcknowledgementMessage, ttl: UByte) {
        logger?.d(LogCategory.LOWER_TRANSPORT) { "Sending $ack" }
        try {
            networkManager.networkLayer.sendAck(pdu = ack, type = PduType.NETWORK_PDU, ttl = ttl)
        } catch (ex: Exception) {
            logger?.w(LogCategory.LOWER_TRANSPORT) { "$ex" }
        }
    }

    /**
     * Sends all unacknowledged segments with the given `sequenceZero` and starts a retransmissions
     * timer.
     * Note: This is an asynchronous method, It will initiate sending the remaining segments and
     * finish immediately.
     *
     * @param sequenceZero The key to get segments from the map.
     */
    private suspend fun sendSegments(sequenceZero: UShort) {
        outgoingSegments[sequenceZero]?.takeIf {
            it.segments.isNotEmpty()
        }?.let {
            val (destination, segments) = it.destination to it.segments
            // The list of segments to be sent.
            //
            // The list contains only unacknowledged segments. Acknowledge segments are set to null
            // when the Segment Acknowledgment message is received.
            //
            // Note: When the destination is a Group or Virtual Address there are no
            //       acknowledgments, in which case all segments are unacknowledged.
            val remainingSegments = segments.unacknowledged()
            sendSegments(remainingSegments)

            // When the last remaining segment has been sent, the lower transport layer should start
            // the SAR Unicast Retransmissions timer or the SAR Multicast Retransmissions timer.
            if (destination is UnicastAddress) {
                startUnicastRetransmissionsTimer(sequenceZero)
            } else {
                startMulticastRetransmissionTimer(sequenceZero)
            }
        }
    }

    /**
     * Sends the given segments one by one with an interval determined by the segment transmission
     * interval.
     *
     * @param segments List of segments to be sent.
     */
    private suspend fun sendSegments(segments: List<SegmentedMessage>) {
        // The interval with which segments are sent by the lower transport layer.
        val segmentTransmissionInterval: Duration =
            networkManager.networkParameters.segmentTransmissionInterval

        // Start sending segments in the same order as they are in the list.
        // Note: Each segment is sent with a delay, therefore each time we check if the network
        //       manager still exists.
        for (segment in segments) {
            // Make sure the network manager is alive.
            segmentTtl[segment.sequenceZero] ?: break

            // Make sure all the segments were not already acknowledged.
            // The this will turn nil when all segments were acknowledged.
            val ttl = requireNotNull(segmentTtl[segment.sequenceZero]) { return }

            // Send the segment and wait for the segment transmission interval.
            try {
                logger?.d(LogCategory.LOWER_TRANSPORT) { "Sending $segment" }
                networkManager.networkLayer.send(
                    pdu = segment,
                    type = PduType.NETWORK_PDU,
                    ttl = ttl
                )
                delay(segmentTransmissionInterval)
            } catch (ex: Exception) {
                logger?.w(LogCategory.LOWER_TRANSPORT) { "$ex" }
                break
            }
        }
    }

    /**
     * Starts the SAR Unicast Retransmissions timer for the message with given [sequenceZero].
     *
     * If the remaining number of retransmissions and the remaining number of retransmissions
     * without progress must be set before the timer is started.
     *
     * @param sequenceZero The key to get segments from the map.
     */
    private suspend fun startUnicastRetransmissionsTimer(sequenceZero: UShort) {
        val remainingNumberOfUnicastRetransmissions =
            requireNotNull(remainingNumberOfUnicastRetransmissions[sequenceZero]) { return }
        val ttl = requireNotNull(segmentTtl[sequenceZero]) { return }

        // Remaining number of retransmissions of segments of an segmented message sent to a Unicast
        // Address. When the number goes to 0 the retransmissions stop.
        val remainingNumberOfRetransmissions = remainingNumberOfUnicastRetransmissions.total
        // Remaining number of retransmissions without progress of segments of an segmented message
        // sent to a Unicast Address. When the number goes to 0 the retransmissions stop.
        val remainingNumberOfUnicastRetransmissionsWithoutProgress =
            remainingNumberOfUnicastRetransmissions.withoutProgress

        // The initial value of the SAR Unicast Retransmissions timer.
        val interval = networkManager.networkParameters.unicastRetransmissionsInterval(ttl)

        // Start the SAR Unicast Retransmissions timer.
        unicastRetransmissionsTimers[sequenceZero] = fixedRateTimer(
            name = "Unicast Retransmission Timer",
            period = interval.inWholeMilliseconds
        ) {
            // The timer has expired, remove it.
            unicastRetransmissionsTimers.remove(sequenceZero)?.also {
                it.cancel()
                it.purge()
            }

            // When the SAR Unicast Retransmissions timer expires and either the remaining number of
            // retransmissions or the remaining number of retransmissions without progress is 0, the
            // lower transport layer shall cancel the transmission of the Upper Transport PDU, shall
            // delete the number of retransmissions value and the number of retransmissions without
            // progress value, shall remove the destination address and the SeqAuth stored for this
            // segmented message, and shall notify the upper transport layer that the transmission
            // of the Upper Transport PDU has timed out.
            require(
                remainingNumberOfRetransmissions > 0u &&
                        remainingNumberOfUnicastRetransmissionsWithoutProgress > 0u
            ) {
                scope.launch {
                    cancelTransmissionOfSegments(sequenceZero, error = null)
                }
                return@fixedRateTimer
            }

            // Decrement both counters. As the SAR Unicast Retransmission timer
            // has expired, no progress has been made.
            this@LowerTransportLayer.remainingNumberOfUnicastRetransmissions[sequenceZero] =
                RemainingNumberOfUnicastRetransmissions(
                    total = (remainingNumberOfRetransmissions - 1u).toUByte(),
                    withoutProgress = (remainingNumberOfUnicastRetransmissionsWithoutProgress - 1u)
                        .toUByte()
                )
            networkManager.scope.launch {
                // Send again unacknowledged segments and restart the timer.
                sendSegments(sequenceZero)
            }
        }
    }

    /**
     * Starts the SAR Multicast Retransmissions timer for the message with given [sequenceZero].
     *
     * If the remaining number of retransmissions must be set before the timer is started.
     *
     * @param sequenceZero The key to get segments from the map.
     */
    private suspend fun startMulticastRetransmissionTimer(sequenceZero: UShort) {
        val remainingNumberOfRetransmissions =
            remainingNumberOfMulticastRetransmissions[sequenceZero] ?: return

        // The initial value of the SAR Multicast Retransmissions timer.
        val interval = networkManager.networkParameters.multicastRetransmissionsInterval

        // Start the SAR Multicast Retransmissions timer.
        multicastRetransmissionsTimers[sequenceZero] = fixedRateTimer(
            name = "Multicast Retransmission Timer",
            period = interval.inWholeMilliseconds
        ) {
            // The timer has expired, remove it.
            multicastRetransmissionsTimers.remove(sequenceZero)?.also {
                it.cancel()
                it.purge()
            }

            // When the SAR Multicast Retransmissions timer expires and the remaining number of
            // retransmissions value is 0, the lower transport layer shall cancel the transmission
            // of the Upper Transport PDU, shall delete the number of retransmissions value and the
            // number of retransmissions without progress value, shall remove the destination
            // address stored for this segmented message, and shall notify the higher layer that the
            // transmission of the Upper Transport PDU has been completed.
            require(remainingNumberOfRetransmissions > 0u) {
                scope.launch {
                    cancelTransmissionOfSegments(sequenceZero, error = null)
                }
                return@fixedRateTimer
            }

            // Decrement the counter.
            remainingNumberOfMulticastRetransmissions[sequenceZero] =
                (remainingNumberOfRetransmissions - 1u).toUByte()
            // Send again unacknowledged segments and restart the timer.
            networkManager.scope.launch {
                sendSegments(sequenceZero)
            }
        }
    }

    /**
     * Removes all timers and counters associated with the message with given [sequenceZero] and
     * notifies the network manager about a success or failure.
     *
     * @param sequenceZero The key to get segments from the map.
     * @param error        Error that caused the transmission to fail or null if the transmission
     */
    private suspend fun cancelTransmissionOfSegments(
        sequenceZero: UShort,
        error: Exception? = null
    ) {
        val (destination, segments) =
            outgoingSegments[sequenceZero] ?: return
        val segment = segments.firstNotAcknowledged() ?: return
        val message = segment.message ?: return
        require(segments.isNotEmpty()) { return }

        remainingNumberOfUnicastRetransmissions.remove(sequenceZero)
        remainingNumberOfMulticastRetransmissions.remove(sequenceZero)
        outgoingSegments.remove(sequenceZero)
        segmentTtl.remove(sequenceZero)

        // Notify user about a timeout only if sending the message was initiated
        // by the user (that means it is not sent as an automatic response to a
        // acknowledged request) and if the message is not acknowledged
        // (in which case the Access Layer may retry).
        if (segment.userInitialized && !message.isAcknowledged) {
            val element = network.localProvisioner?.node?.element(
                address = segment.source
            ) ?: return

            error?.let {
                networkManager.emitNetworkManagerEvent(
                    NetworkManagerEvent.MessageSendingFailed(
                        message = message,
                        localElement = element,
                        destination = destination,
                        error = it
                    )
                )
            } ?: run {
                networkManager.emitNetworkManagerEvent(
                    NetworkManagerEvent.MessageSent(
                        message = message,
                        localElement = element,
                        destination = destination
                    )
                )
            }
        }

        networkManager.upperTransportLayer.onLowerTransportLayerSent(
            destination = destination.address
        )
    }
}

/**
 * Class containing the remaining number of unicast retransmissions.
 *
 * @property total           Remaining number of retransmissions of segmented message sent to a
 *                           Unicast Address. When the number goes to the 0 teh retransmissions
 *                           stop.
 * @property withoutProgress Remaining number of retransmissions without progress of segments of a
 *                           segmented message sent to a Unicast Address. When the number foes to 0
 *                           the retransmission stops.
 */
internal data class RemainingNumberOfUnicastRetransmissions(
    val total: UByte,
    val withoutProgress: UByte
)

/**
 * Class that defines an outgoing segment
 *
 * @property destination Destination address.
 * @property segments    Segments to be sent.
 * @constructor creates an OutgoingSegment object.
 */
internal data class OutgoingSegment(
    val destination: MeshAddress,
    val segments: MutableList<SegmentedMessage?>
)