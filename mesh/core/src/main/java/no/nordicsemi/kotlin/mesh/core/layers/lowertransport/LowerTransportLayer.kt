package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.layers.NetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.HeartbeatMessage
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.UpperTransportPdu
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.logger.LogCategory

internal class LowerTransportLayer(private val networkManager: NetworkManager) {

    private val logger = networkManager.logger
    fun send(pdu: UpperTransportPdu, initialTtl: UByte?, networkKey: NetworkKey) {
        // TODO()
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
                lowerTransportPdu = message,
                type = PduType.NETWORK_PDU,
                ttl = heartbeat.initialTtl
            )
        } catch (ex: Exception) {
            logger?.e(LogCategory.LOWER_TRANSPORT) { "$ex" }
        }
    }

    fun isReceivingMessage(address: Address): Boolean {
        // TODO
        return false
    }
}