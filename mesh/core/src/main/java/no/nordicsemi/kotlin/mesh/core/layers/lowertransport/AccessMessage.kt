@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * Data class defining an Access message.
 *
 * @property aid               6-bit Application key identifier. This field is set to null if the
 *                             message is signed with a Device Key instead.
 * @property sequence          Sequence number used to encode this message.
 * @property transportMicSize  Size of the transport MIC which is 4 or 8 bytes.
 * @constructor Creates an Access Message.
 */
internal data class AccessMessage(
    override val source: MeshAddress,
    override val destination: MeshAddress,
    override val networkKey: NetworkKey,
    override val ivIndex: UInt,
    override val upperTransportPdu: ByteArray
) : LowerTransportPdu {

    override val type = LowerTransportPduType.ACCESS_MESSAGE
    val aid: UByte? = null
    val sequence: UInt? = null
    var transportMicSize: UByte? = null
    override val transportPdu: ByteArray
        get() {
            val octet0: UByte = aid?.let { aid ->
                0.toUByte() or 0b01000000.toUByte() or aid
            } ?: 0.toUByte()
            return ByteArray(octet0.toInt()) + upperTransportPdu
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessMessage

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
