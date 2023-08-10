@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.messages.proxy.ProxyConfigurationMessage
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.UnassignedAddress
import kotlin.experimental.and

/**
 * Data class defining a Control Message.
 *
 * @property opCode             Message Op Code.
 * @property ttl                TTL value of the message.
 * @constructor Creates a Control Message.
 */
internal data class ControlMessage(
    val opCode: UByte,
    override val source: MeshAddress,
    override val destination: MeshAddress,
    override val networkKey: NetworkKey,
    override val ivIndex: UInt,
    override val upperTransportPdu: ByteArray,
    val ttl: UByte
) : LowerTransportPdu {

    override val transportPdu: ByteArray
        get() = byteArrayOf() + opCode.toByte() + upperTransportPdu

    override val type = LowerTransportPduType.CONTROL_MESSAGE


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ControlMessage

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (networkKey != other.networkKey) return false
        if (ivIndex != other.ivIndex) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false
        if (!upperTransportPdu.contentEquals(other.upperTransportPdu)) return false
        if (type != other.type) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + networkKey.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + transportPdu.contentHashCode()
        result = 31 * result + upperTransportPdu.contentHashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

internal object ControlMessageDecoder {

    /**
     * Decodes the given [NetworkPdu] to a [ControlMessage].
     *
     * @param networkPdu The network pdu to be decoded.
     * @return ControlMessage or null if the pdu could not be decoded.
     */
    fun decode(networkPdu: NetworkPdu) = networkPdu.takeIf {
        it.transportPdu.isNotEmpty() && it.transportPdu[0] and 0x00.toByte() == 0x00.toByte()
    }?.let {
        ControlMessage(
            opCode = (it.transportPdu[0] and 0x7F.toByte()).toUByte(),
            upperTransportPdu = it.transportPdu.drop(1).toByteArray(),
            source = it.source,
            destination = it.destination,
            networkKey = it.key,
            ivIndex = it.ivIndex,
            ttl = it.ttl
        )
    }

    /**
     * Decodes the given [NetworkPdu] to a [ControlMessage].
     *
     * @param segments Array of [SegmentedControlMessage] to be decoded.
     * @return decoded ControlMessage.
     */
    fun decode(segments: Array<SegmentedControlMessage>): ControlMessage {
        val upperTransportPdu = segments.fold(byteArrayOf()) { acc, segment ->
            acc + segment.upperTransportPdu
        }
        return segments.first().run {
            ControlMessage(
                opCode = opCode,
                upperTransportPdu = upperTransportPdu,
                source = source,
                destination = destination,
                networkKey = networkKey,
                ivIndex = ivIndex,
                ttl = ttl
            )
        }
    }

    /**
     * Creates a Control Message from the given Proxy configuration message. The source should be
     * set to the local Node address. The given Network Key should be known to the Proxy node.
     *
     * @param message      Proxy configuration message.
     * @param source       Source address of the message.
     * @param networkKey   Network key to be used to encrypt the message.
     * @param ivIndex      Current IV Index of the network.
     *
     * @return decoded ControlMessage.
     */
    fun decode(
        message: ProxyConfigurationMessage,
        source: MeshAddress,
        networkKey: NetworkKey,
        ivIndex: IvIndex
    ) = ControlMessage(
        opCode = message.opCode,
        upperTransportPdu = message.parameters ?: byteArrayOf(),
        source = source,
        destination = UnassignedAddress,
        networkKey = networkKey,
        ivIndex = ivIndex.transmitIvIndex,
        ttl = 0u
    )
}

