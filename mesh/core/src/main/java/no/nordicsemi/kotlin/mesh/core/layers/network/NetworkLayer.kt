package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.logger.LogCategory

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

    fun handle(incomingPdu: ByteArray, type: PduType) {
        // Discard provisioning pdus as they are handled by the provisioning manager
        if (type == PduType.PROVISIONING_PDU) return

        if (type != PduType.MESH_BEACON) {
            // TODO Ensure the PDU has not been handled already
        }

        when (type) {
            PduType.NETWORK_PDU -> {
                NetworkPduDecoder.decode(incomingPdu, type, meshNetwork)?.let { networkPdu ->
                    logger?.i(LogCategory.NETWORK) { "$networkPdu received" }
                    // TODO networkManager.lowerTransportLayer.handle(networkPdu)
                } ?: logger?.w(LogCategory.NETWORK) { "Unable to decode network pdu" }
            }
            PduType.MESH_BEACON -> {/*TODO Mesh Beacon decoder*/
            }


            PduType.PROXY_CONFIGURATION -> { /* TODO Proxy configuration decoder*/
            }

            else -> return
        }
    }
}