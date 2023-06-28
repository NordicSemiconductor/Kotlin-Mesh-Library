@file:Suppress("unused", "UNUSED_PARAMETER")

package no.nordicsemi.kotlin.mesh.core.layers

import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.bearer.Transmitter
import no.nordicsemi.kotlin.mesh.core.MeshNetworkManager
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkLayer
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.HasAddress
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.logger.Logger
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration
import kotlin.time.DurationUnit

/**
 * Network Manager contains the different layers of the mesh network architecture.
 *
 * @property manager Mesh network manager
 * @constructor Constructs the network manager.
 */
internal class NetworkManager(private val manager: MeshNetworkManager) {
    var logger: Logger? = null

    var networkLayer = NetworkLayer(this)
    // var lowerTransportLayer = LowerTransportLayer(this)
    // var upperTransportLayer = UpperTransportLayer(this)
    // var accessLayer = AccessLayer(this)

    var transmitter: Transmitter? = manager.transmitter

    var meshNetwork = manager.meshNetwork.replayCache.first()

    var networkParameters = NetworkParameters()


    fun handle(incomingPdu: ByteArray, type: PduType) {
        networkLayer.handle(incomingPdu = incomingPdu, type = type)
    }

    /**
     * Publishes the given message using the Publish information from the given Model. If
     * publication is not set, this message does nothing.
     *
     * If publication retransmission is set, this method will retransmit the message specified
     * number of times, if applicable keeps the same TID value.
     *
     * @param message     Message to be published.
     * @param from        Source model from which the message is originating from.
     */
    fun publish(message: MeshMessage, from: Model) {
        val publish = from.publish ?: return
        val localElement = from.parentElement ?: return
        val applicationKey = meshNetwork.applicationKeys[publish.index]

        // calculate the TTL to be used
        val ttl = when (publish.ttl != 0xFF) {
            true -> publish.ttl.toUByte()
            false -> localElement.parentNode?.defaultTTL?.toUByte() ?: networkParameters.defaultTtl
        }

        send(message, localElement, publish.address, ttl, applicationKey)

        if (message is AcknowledgedMeshMessage) {
            var count = publish.retransmit.count.toInt()
            if (count > 0) {
                val interval: Duration = publish.retransmit.interval
                fixedRateTimer(
                    daemon = false,
                    period = interval.toLong(DurationUnit.MILLISECONDS)
                ) {
                    when (--count > 0) {
                        true -> send(message, localElement, publish.address, ttl, applicationKey)
                        false -> cancel()
                    }
                }
            }
        }
    }

    /**
     * Encrypts the message with the Application Key and a Network Key bound to it, and sends to the
     * given destination address.
     *
     * This method does not send nor return PDUs to be sent. Instead, for each created segment it
     * calls transmitter's ``Transmitter/send(_:ofType:)`` method, which should send the PDU over
     * the air. This is in order to support retransmission in case a packet was lost and needs to be
     * sent again after block acknowledgment was received.
     *
     * @param message         Message to be sent.
     * @param element         Source Element from which the message is originating from.
     * @param destination     Destination address.
     * @param ttl             Initial TTL (Time To Live) value of the message. If `nil`, default
     *                        Node TTL will be used.
     * @param key    Application Key to sign the message.
     */
    fun send(
        message: MeshMessage,
        element: Element,
        destination: HasAddress,
        ttl: UByte?,
        key: ApplicationKey
    ) {
        // TODO accessLayer.send(message, srcElement, destination, withTtl, using)
    }
}