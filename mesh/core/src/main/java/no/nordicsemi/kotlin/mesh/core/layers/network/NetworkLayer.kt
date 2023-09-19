@file:Suppress("MemberVisibilityCanBePrivate", "unused", "UNUSED_PARAMETER")

package no.nordicsemi.kotlin.mesh.core.layers.network

import kotlinx.datetime.Clock
import no.nordicsemi.kotlin.mesh.bearer.Bearer
import no.nordicsemi.kotlin.mesh.bearer.BearerError
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.ProxyFilter
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.AccessMessage
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.ControlMessage
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportPdu
import no.nordicsemi.kotlin.mesh.core.messages.proxy.FilterStatus
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.KeyDistribution
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UsingNewKeys
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.model.boundTo
import no.nordicsemi.kotlin.mesh.core.model.maxUnicastAddress
import no.nordicsemi.kotlin.mesh.core.next
import no.nordicsemi.kotlin.mesh.core.reset
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import kotlin.concurrent.fixedRateTimer

/**
 * Network Layer of the mesh networking stack
 *
 * @property networkManager Network manager containing the different layers of the mesh networking
 *                          stack.
 * @constructor Constructs the network layer.
 */
internal class NetworkLayer(private val networkManager: NetworkManager) {

    private val meshNetwork = networkManager.meshNetwork
    private val logger = networkManager.logger
    private var proxyNetworkKey: NetworkKey? = null
    private val networkMessageCache = mutableMapOf<ByteArray, Any?>()

    /**
     * This method handles the received PDU of given type and passes it to Upper Transport Layer.
     *
     * @param incomingPdu  Data received.
     * @param type         PDU type.
     */
    suspend fun handle(incomingPdu: ByteArray, type: PduType) {
        // Discard provisioning pdus as they are handled by the provisioning manager
        if (type == PduType.PROVISIONING_PDU) return

        // Secure Network Beacons can repeat whenever the device connects to a new Proxy.
        if (type != PduType.MESH_BEACON) {
            // Ensure the PDU has not been handled already.
            require(networkMessageCache[incomingPdu] == null) {
                logger?.d(LogCategory.NETWORK) { "PDU already handled." }
                return
            }
            networkMessageCache[incomingPdu] = null
        }

        // Try decoding the pdu.
        when (type) {
            PduType.NETWORK_PDU -> {
                NetworkPduDecoder.decode(incomingPdu, type, meshNetwork)?.let { networkPdu ->
                    logger?.i(LogCategory.NETWORK) { "$networkPdu received." }
                } ?: logger?.w(LogCategory.NETWORK) { "Unable to decode network pdu." }
            }

            PduType.MESH_BEACON -> {
                NetworkBeaconPduDecoder.decode(incomingPdu, meshNetwork)?.let { networkBeaconPdu ->
                    logger?.i(LogCategory.NETWORK) { "$networkBeaconPdu received" }
                    handle(networkBeaconPdu)
                    return
                }
                UnprovisionedDeviceBeaconDecoder.decode(incomingPdu)?.let { unprovisionedBeacon ->
                    logger?.i(LogCategory.NETWORK) { "$unprovisionedBeacon received." }
                    handle(unprovisionedBeacon)
                    return
                }
                logger?.w(LogCategory.NETWORK) { "Failed to decrypt mesh beacon pdu." }
            }

            PduType.PROXY_CONFIGURATION -> {
                NetworkPduDecoder.decode(incomingPdu, type, meshNetwork)?.let { proxyPdu ->
                    logger?.i(LogCategory.NETWORK) { "$proxyPdu received." }
                    handle(proxyPdu)
                    return
                }
                logger?.w(LogCategory.NETWORK) { "Unable to decode network pdu." }
            }

            else -> return
        }
    }

    /**
     * This method tries to send the Lower Transport Message of given type to the given destination
     * address. If the local Provisioner does not exist, or does not have Unicast Address assigned,
     * this method does nothing.
     *
     * @param lowerTransportPdu  Lower Transport PDU to be sent.
     * @param type               PDU type.
     * @param ttl                Initial TTL (Time To Live) value of the message.
     * @throws BearerError.Closed when the bearer is closed.
     */
    @Throws(BearerError.Closed::class)
    suspend fun send(lowerTransportPdu: LowerTransportPdu, type: PduType, ttl: UByte) {
        networkManager.transmitter?.let { transmitter ->
            val sequence = (lowerTransportPdu as AccessMessage).sequence
            val networkPdu = NetworkPduDecoder.encode(
                lowerTransportPdu = lowerTransportPdu,
                pduType = type,
                sequence = sequence,
                ttl = ttl
            )
            logger?.i(LogCategory.NETWORK) {
                "Sending $networkPdu encrypted using ${networkPdu.key}"
            }
            // Loopback interface
            if (shouldLoopback(networkPdu = networkPdu)) {
                handle(incomingPdu = networkPdu.pdu, type = type)
                // Messages sent with TTL = 1 will only be sent locally.
                require(ttl == 1.toUByte()) { return }
                if (isLocalUnicastAddress(networkPdu.destination as UnicastAddress)) {
                    // No need to send messages targeting local Unicast Addresses.
                    return
                }
                // If the message was sent locally, don't report Bearer closer error.
                transmitter.send(pdu = networkPdu.pdu, type = type)
            } else {
                // Messages sent with TTL = 1 will only be sent locally.
                require(ttl == 1.toUByte()) { return }
                try {
                    transmitter.send(pdu = networkPdu.pdu, type = type)
                } catch (exception: Exception) {
                    if (exception is BearerError.Closed) {
                        proxyNetworkKey = null
                    }
                    throw exception
                }
            }

            // Unless a GATT Bearer is used, the Network PDUs should be sent multiple times if
            // Network Transmit has been set for the local Provisioner's Node
            if (type == PduType.NETWORK_PDU && transmitter is Bearer) {
                meshNetwork.localProvisioner?.node?.networkTransmit?.takeIf {
                    it.count > 1u
                }?.let { networkTransmit ->
                    var count = networkTransmit.count.toInt()
                    fixedRateTimer(period = networkTransmit.intervalAsMilliseconds) {
                        // networkManager.transmitter?.send(pdu = networkPdu.pdu, type = type)
                        count -= 1
                        if (count == 0)
                            cancel()
                    }
                }
            }
        } ?: throw BearerError.Closed
    }

    /**
     * Sends the Proxy Configuration Message. The Proxy Filter object will be notified about the
     * success or a failure.
     *
     * @param message The Proxy Configuration message to be sent.
     */
    suspend fun send(message: ProxyConfigurationMessage) {
        proxyNetworkKey?.let { networkKey ->
            val source = meshNetwork.localProvisioner?.node?.primaryUnicastAddress
                ?: UnicastAddress(address = maxUnicastAddress)
            logger?.i(LogCategory.PROXY) {
                "Sending $message from: ${source.toHex()} to :0000 "
            }
            val pdu = ControlMessage.init(
                message = message,
                source = source,
                networkKey = networkKey,
                ivIndex = meshNetwork.ivIndex
            )
            logger?.i(LogCategory.NETWORK) { "Sending $pdu" }

            try {
                send(lowerTransportPdu = pdu, type = PduType.PROXY_CONFIGURATION, ttl = pdu.ttl)
                networkManager.proxy.onManagerDidDeliverMessage(message)
            } catch (exception: Exception) {
                if (exception is BearerError.Closed) {
                    proxyNetworkKey = null
                }
                networkManager.proxy.onManagerFailedToDeliverMessage(message, exception)
            }
        }
        networkManager.proxy.onManagerFailedToDeliverMessage(message, BearerError.Closed)
    }

    /**
     * Returns the next outgoing sequence number for the given local source address.
     *
     * @param address Local source address.
     */
    suspend fun nextSequenceNumber(address: UnicastAddress): UInt =
        networkManager.networkPropertiesStorage.next(meshNetwork.uuid, address)

    /**
     * This method handles the Unprovisioned Device beacon. The current implementation does nothing,
     * as remote provisioning is currently not supported.
     *
     * @param beacon Received Unprovisioned Device beacon.
     *
     */
    private fun handle(beacon: UnprovisionedDeviceBeacon) {
        // TODO Handle unprovisioned device beacon
    }

    /**
     * This method handles PDUs containing network state.
     *
     * As of Mesh Protocol 1.1 these are Secure Network beacons and Private beacons. These beacons
     * will set the IV Index and IV Update Active flag and change the Key Refresh Phase based on the
     * information specified in them.
     */
    private suspend fun handle(networkBeacon: NetworkBeaconPdu) {
        // The network key the beacon was authenticated with.
        val networkKey = networkBeacon.networkKey

        if (meshNetwork.primaryNetworkKey != null && !networkKey.isPrimary) {
            logger?.w(LogCategory.NETWORK) {
                "Discarding beacon for secondary network (key index: ${networkKey.index})"
            }

            if (proxyNetworkKey == null) {
                updateProxyFilter(networkKey)
                return
            }
        }

        val lastIvIndex = networkManager.networkPropertiesStorage.ivIndex
        val lastTransitionDate = networkManager.networkPropertiesStorage.lastTransitionDate
        val isIvRecoveryActive = networkManager.networkPropertiesStorage.isIvRecoveryActive

        val isIvTestModeActive = networkManager.networkParameters.ivUpdateTestMode
        val flag = networkManager.networkParameters.allowIvIndexRecoveryOver42

        if (networkBeacon.canOverWrite(
                target = lastIvIndex,
                updatedAt = lastTransitionDate,
                isIvRecoveryActive = isIvRecoveryActive,
                isIvTestModeActive = isIvTestModeActive,
                ivRecoveryOver42Allowed = flag
            )
        ) {
            meshNetwork.ivIndex = networkBeacon.ivIndex
            if (meshNetwork.ivIndex.index > lastIvIndex.index) {
                logger?.i(LogCategory.NETWORK) {
                    "Applying ${meshNetwork.ivIndex}"
                }
            }
            meshNetwork.let {
                if (it.localProvisioner?.node != null &&
                    it.ivIndex.transmitIvIndex > lastIvIndex.transmitIvIndex
                ) {
                    logger?.i(LogCategory.NETWORK) { "Resetting local sequence numbers to 0" }
                    networkManager.networkPropertiesStorage.reset(
                        uuid = meshNetwork.uuid, node = it.localProvisioner!!.node!!
                    )
                }
            }
            // Store the last IV Index
            networkManager.networkPropertiesStorage.ivIndex = meshNetwork.ivIndex
            if (lastIvIndex != meshNetwork.ivIndex) {
                networkManager.networkPropertiesStorage.lastTransitionDate = Clock.System.now()
                val ivRecovery = meshNetwork.ivIndex.index > lastIvIndex.index + 1u &&
                        !networkBeacon.ivIndex.isIvUpdateActive
                networkManager.networkPropertiesStorage.isIvRecoveryActive = ivRecovery
            }

            // If the Key Refresh procedure is in progress, and the new Network Key has already been
            // set, the key refresh flag indicates switching to phase 2
            if (networkKey.phase is KeyDistribution &&
                networkBeacon.validForKeyRefreshProcedure &&
                !networkBeacon.keyRefreshFlag
            ) {
                networkKey.phase = UsingNewKeys
            }

            // if the Key Refresh Procedure is in Phase 2, and the key refresh flag is set to false.
            if (networkKey.phase is UsingNewKeys &&
                networkBeacon.validForKeyRefreshProcedure &&
                networkBeacon.keyRefreshFlag
            ) {
                // Revoke the old network key
                networkKey.oldKey = null // This will set the phase to NormalOperation
                // ...and old application keys bound to it
                meshNetwork._applicationKeys.boundTo(networkKey).forEach { it.oldKey = null }

            }

        } else if (networkBeacon.ivIndex != lastIvIndex.previous) {
            val numberOfHoursSinceData = "${(Clock.System.now() - lastTransitionDate).inWholeHours}"
            logger?.w(LogCategory.NETWORK) {
                "Discarding beacon (${networkBeacon.ivIndex}, " +
                        "last ${lastIvIndex}, changed $numberOfHoursSinceData hours ago, " +
                        "test mode: ${networkManager.networkParameters.ivUpdateTestMode})"
            }
            return
        } // else,

        // The beacon was sent by a Node with a previous IV Index, that was not yet transition to
        // the one the local node has. Such an IV Index is still valid, at least for sometime.

        updateProxyFilter(networkKey)
    }

    /**
     * Updates the information about the Network Key known to the current Proxy Server. The Network
     * Key is required to send proxy Configuration Messages that can be decoded by the connected
     * Proxy.
     *
     * For new Proxy connections this method also initiates the Proxy Filter with preset.
     *
     * @param networkKey The Network Key known to the connected Proxy Server.
     */
    private suspend fun updateProxyFilter(networkKey: NetworkKey) {
        val justConnected = proxyNetworkKey == null

        // Keep the primary Network Key or the most recently received one from the connected Proxy
        // Server. This is to make sure (almost) that the Proxy Configuration messages are sent
        // encrypted with a key known to this Node.
        proxyNetworkKey = networkKey

        if (justConnected) {
            networkManager.proxy.newProxyDidConnect()
        }
    }

    /**
     * Handles the received Proxy Configuration PDU. This method parses the payload and instantiates
     * a message class. The message is passed to the [ProxyFilter] for processing.
     *
     * @param proxyPdu Received Proxy Configuration PDU.
     */
    private suspend fun handle(proxyPdu: NetworkPdu) {
        val payload = proxyPdu.transportPdu
        require(payload.size > 1) { return }

        val controlMessage = ControlMessage.init(proxyPdu) ?: run {
            logger?.w(LogCategory.NETWORK) { "Failed to decrypt proxy PDU" }
            return
        }
        logger?.i(LogCategory.NETWORK) {
            "$controlMessage received (decrypted using key: ${controlMessage.networkKey}"
        }

        when (controlMessage.opCode) {
            FilterStatus.opCode -> FilterStatus.Initializer
            else -> null
        }?.let { decoder ->
            decoder.init(parameters = controlMessage.upperTransportPdu)?.also { message ->
                logger?.i(LogCategory.PROXY) {
                    "$message received from: ${proxyPdu.source.toHex()} to ${proxyPdu.destination.toHex()}"
                }
                // Look for the proxy Node.
                val proxyNode = meshNetwork.node(proxyPdu.source as UnicastAddress)
                networkManager.proxy.handle(message = message, proxy = proxyNode)
            }
        } ?: run {
            logger?.w(LogCategory.PROXY) { "Unknown Proxy Configuration message (opcode: ${controlMessage.opCode})" }
        }
    }

    /**
     * Check whether the given address is an address of an element belonging to the local Node.
     *
     * @param address Address to check.
     * @return `true` if the address belongs to an element in the local Node or `false` otherwise.
     */
    private fun isLocalUnicastAddress(address: UnicastAddress) =
        meshNetwork.localProvisioner?.node?.containsElementWithAddress(address) ?: false

    /**
     * Check if the given [NetworkPdu] should loop back for local processing.
     *
     * @param networkPdu Network PDU to check.
     * @return `true` if the PDU should be looped back or `false` otherwise.
     */
    private fun shouldLoopback(networkPdu: NetworkPdu) = networkPdu.destination.let {
        it is GroupAddress || it is VirtualAddress || isLocalUnicastAddress(it as UnicastAddress)
    }
}

