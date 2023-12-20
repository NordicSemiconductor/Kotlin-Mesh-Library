@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.layers.uppertransport

import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.ControlMessage
import no.nordicsemi.kotlin.mesh.core.model.Features
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatDestination
import no.nordicsemi.kotlin.mesh.core.model.HeartbeatPublication
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.util.Utils.toUShort


/**
 * Defines a HeartbeatMessage
 *
 * @property opCode          Opcode of the message.
 * @property initialTtl      Initial TTL value of the message.
 * @property features        Currently active features of the Node.
 * @property source          Source address of the message.
 * @property destination     Destination address of the message.
 * @property receivedTtl     Received TTL value of the message.
 * @property ivIndex         IV Index of the message.
 * @property transportPdu    Transport PDU of the message.
 * @property hops            Number of hops the message has taken.
 * @constructor Creates a Heartbeat message.
 */
internal data class HeartbeatMessage(
    val opCode: UByte,
    val initialTtl: UByte,
    val features: Features,
    val source: MeshAddress,
    val destination: HeartbeatDestination,
    val receivedTtl: UByte?,
    val ivIndex: UInt,
    val transportPdu: ByteArray
) {

    val hops: UByte
        get() = receivedTtl?.let {
            (initialTtl + 1u - it).toUByte()
        } ?: 0u

    override fun toString() = receivedTtl?.let { receivedTtl ->
        "Heartbeat Message (initial TTL: $initialTtl, received TTL: $receivedTtl, " +
                "hops: $hops, features: $features"
    } ?: "Heartbeat Message (initial TTL: $initialTtl, features: $features"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HeartbeatMessage

        if (opCode != other.opCode) return false
        if (initialTtl != other.initialTtl) return false
        if (features != other.features) return false
        if (source != other.source) return false
        if (destination != other.destination) return false
        if (receivedTtl != other.receivedTtl) return false
        if (ivIndex != other.ivIndex) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = opCode.hashCode()
        result = 31 * result + initialTtl.hashCode()
        result = 31 * result + features.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + receivedTtl.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + transportPdu.contentHashCode()
        return result
    }

    internal companion object {
        val OP_CODE: UByte = 0x0Au

        /**
         * Constructs a HeartbeatMessage from the given ControlMessage.
         *
         * @param message ControlMessage containing the HeartbeatMessage.
         * @return HeartbeatMessage or null if the message is invalid.
         */
        fun init(message: ControlMessage) = message.takeIf {
            it.opCode == OP_CODE && message.upperTransportPdu.size == 3
        }?.let {
            HeartbeatMessage(
                opCode = it.opCode,
                initialTtl = it.ttl,
                features = Features(rawValue = it.upperTransportPdu.toUShort(1)),
                source = it.source,
                destination = it.destination as HeartbeatDestination,
                receivedTtl = it.ttl,
                ivIndex = it.ivIndex,
                transportPdu = it.upperTransportPdu
            )
        }

        /**
         * Constructs a HeartbeatMessage from the given HeartbeatPublication.
         *
         * @param heartbeatPublication         HeartbeatPublication containing the HeartbeatMessage.
         * @param source                       Source address of the HeartbeatMessage.
         * @param destination                  Destination address of the HeartbeatMessage.
         * @param ivIndex                      Current IV Index.
         * @return HeartbeatMessage or null if the message is invalid.
         */
        fun init(
            heartbeatPublication: HeartbeatPublication,
            source: MeshAddress,
            destination: HeartbeatDestination,
            ivIndex: IvIndex
        ) = HeartbeatMessage(
            opCode = OP_CODE,
            initialTtl = heartbeatPublication.ttl,
            features = Features(0u),
            source = source,
            destination = destination,
            receivedTtl = null,
            ivIndex = ivIndex.index,
            transportPdu = byteArrayOf((heartbeatPublication.ttl and 0x7Fu).toByte())
        )
    }
}