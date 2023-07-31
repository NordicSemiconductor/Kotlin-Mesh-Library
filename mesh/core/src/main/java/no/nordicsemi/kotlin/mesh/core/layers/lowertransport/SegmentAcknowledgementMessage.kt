@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

/**
 * Internal data class defining a Segment Acknowledgement Message.
 *
 * @property opCode                    Message Op Code.
 * @property isOnBehalfOfLowePowerNode Flag indicating whether the message is on behalf of a low
 *                                     power node.
 * @property sequenceZero              13 least significant bits of SeqAuth.
 * @property blockAck                  Block acknowledgement for segments, bit field.
 * @property isBusy                    Flag indicating whether the node is already busy processing a
 *                                     message and should ignore any incoming messages.
 */
internal data class SegmentAcknowledgementMessage(
    val opCode: UByte,
    override val source: MeshAddress,
    override val destination: MeshAddress,
    override val networkKey: NetworkKey,
    override val ivIndex: UInt,
    override val upperTransportPdu: ByteArray,
    val isOnBehalfOfLowePowerNode: Boolean = false, // Friend feature not supported
    val sequenceZero: UShort,
    val blockAck: UInt
) : LowerTransportPdu {

    override val type = LowerTransportPduType.CONTROL_MESSAGE

    val isBusy: Boolean
        get() = blockAck == 0u
    override val transportPdu: ByteArray
        get() {
            val octet0 = opCode and 0x7F.toUByte()
            val octet1 = if (isOnBehalfOfLowePowerNode)
                0x80.toUByte() else 0x00.toUByte() or (sequenceZero.toInt() shr 6).toUByte()
            val octet2 = ((sequenceZero.toInt() and 0x3F) shl 2).toUByte()
            return byteArrayOf(octet0.toByte(), octet1.toByte(), octet2.toByte()) +
                    upperTransportPdu
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentAcknowledgementMessage

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (networkKey != other.networkKey) return false
        if (ivIndex != other.ivIndex) return false
        if (!upperTransportPdu.contentEquals(other.upperTransportPdu)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + networkKey.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + upperTransportPdu.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    /**
     * Checks if the segment with the given sequence number has been received.
     *
     * @param m Segment number.
     * @return true if the segment has been received or false otherwise.
     */
    fun isSegmentReceived(m: Int) = blockAck and (1 shl m).toUInt() != 0.toUInt()

    /**
     * Checks if all segments have been received.
     *
     * @param segments List of segmented messages.
     * @return true if all segments have been received or false otherwise.
     */
    fun areAllSegmentsReceived(segments: List<SegmentedMessage>): Boolean =
        areAllSegmentsReceived(segments.size - 1)

    /**
     * Checks if all segments have been received.
     *
     * @param lastSegmentNumber Last segment number.
     * @return true if all segments have been received or false otherwise.
     */
    fun areAllSegmentsReceived(lastSegmentNumber: Int) =
        blockAck == ((1 shl lastSegmentNumber) - 1).toUInt()

    override fun toString() = "ACK (seqZero: $sequenceZero, blockAck: " +
            "${blockAck.toByteArray().encodeHex(true)})"

    internal object Decoder {

        /**
         * Decodes the given [NetworkPdu] containing a segmented acknowledgement message.
         *
         * @param networkPdu The network pdu containing the segment acknowledgement message.
         * @return The decoded [SegmentAcknowledgementMessage] or null if the pdu was invalid.
         */
        fun decode(networkPdu: NetworkPdu): SegmentAcknowledgementMessage? {
            networkPdu.run {
                require(
                    transportPdu.size == 7 &&
                            transportPdu[0].toUByte() and 0x80.toUByte() == 0x00.toUByte()
                ) { return null }

                val opCode = transportPdu[0].toUByte() and 0x7F.toUByte()
                require(opCode == 0x00.toUByte()) { return null }
                val isOnBehalfOfLowePowerNode = opCode and 0x80.toUByte() == 0x80.toUByte()
                val sequenceZero = ((transportPdu[1].toUByte() and 0x3F.toUByte()).toInt() shl 6) or
                        ((transportPdu[2].toUByte() and 0xFC.toUByte()).toInt() shr 2)
                val blockAck = ((transportPdu[2].toUByte() and 0x03.toUByte()).toInt() shl 8) or
                        transportPdu[3].toUByte().toInt()
                val upperTransportPdu = transportPdu.copyOfRange(4, transportPdu.size)
                return SegmentAcknowledgementMessage(
                    opCode = opCode,
                    source = source,
                    destination = destination,
                    networkKey = key,
                    ivIndex = networkPdu.ivIndex,
                    upperTransportPdu = upperTransportPdu,
                    isOnBehalfOfLowePowerNode = isOnBehalfOfLowePowerNode,
                    sequenceZero = sequenceZero.toUShort(),
                    blockAck = blockAck.toUInt()
                )
            }
        }

        /**
         * Decodes the given [SegmentedMessage]s containing a segmented acknowledgement message.
         *
         * @param segments List of segmented messages containing the segment acknowledgement
         *                 message.
         * @return The decoded [SegmentAcknowledgementMessage] or null if the pdu was invalid.
         */
        fun decode(segments: List<SegmentedMessage>): SegmentAcknowledgementMessage {
            val segmentAck = segments.first { it.isSegmented }
            val segment = segments.first { it.sequenceZero == segmentAck.sequenceZero }

            var ack = 0u
            segments.forEach { ack = ack or ((1 shl it.segmentOffset.toInt()).toUInt()) }

            return SegmentAcknowledgementMessage(
                opCode = 0x00u,
                source = segment.source,
                destination = segment.destination,
                networkKey = segment.networkKey,
                ivIndex = segment.ivIndex,
                upperTransportPdu = segment.upperTransportPdu,
                sequenceZero = segment.sequenceZero,
                blockAck = ack
            )
        }
    }
}
