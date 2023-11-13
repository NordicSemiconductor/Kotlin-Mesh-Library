@file:Suppress("MemberVisibilityCanBePrivate", "unused")
@file:OptIn(ExperimentalStdlibApi::class)

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.UpperTransportPdu
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

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
    override val upperTransportPdu: ByteArray,
    val transportMicSize: UByte,
    val sequence: UInt,
    val aid: UByte? = null
) : LowerTransportPdu {

    override val type = LowerTransportPduType.ACCESS_MESSAGE
    override val transportPdu: ByteArray
        get() {
            var octet0 = 0.toUByte()
            aid?.let {
                octet0 = octet0 or 0b01000000.toUByte()
                octet0 = octet0 or it
            }
            return byteArrayOf(octet0.toByte()) + upperTransportPdu
        }

    /**
     * Creates an Access Message from the given Network PDU and Network Key.
     *
     * @param pdu          Network PDU containing the access message.
     * @param networkKey   Network key used to decode/encode the PDU.
     * @constructor Creates an Access Message.
     */
    internal constructor(
        pdu: UpperTransportPdu,
        networkKey: NetworkKey
    ) : this(
        source = MeshAddress.create(pdu.source),
        destination = pdu.destination,
        networkKey = networkKey,
        ivIndex = pdu.ivIndex,
        upperTransportPdu = pdu.transportPdu,
        transportMicSize = pdu.transportMicSize,
        sequence = pdu.sequence,
        aid = pdu.aid
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AccessMessage

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (networkKey != other.networkKey) return false
        if (ivIndex != other.ivIndex) return false
        if (!upperTransportPdu.contentEquals(other.upperTransportPdu)) return false
        if (transportMicSize != other.transportMicSize) return false
        if (sequence != other.sequence) return false
        if (aid != other.aid) return false
        if (type != other.type) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + networkKey.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + upperTransportPdu.contentHashCode()
        result = 31 * result + transportMicSize.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + (aid?.hashCode() ?: 0)
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString() = "$type (akf: ${if (aid != null) "1, " +
            "aid: ${aid.toHexString()}" else "0"}, " +
            "szmic: ${if (transportMicSize == 4.toUByte()) 0 else 1}, " +
            "data: ${upperTransportPdu.encodeHex()}"

    internal companion object {

        /**
         * Creates an Access Message from the given Network PDU.
         *
         * @param pdu Network PDU containing the access message.
         * @return an AccessMessage or null if the pdu is invalid.
         */
        fun init(pdu: NetworkPdu) = pdu.takeIf {
            it.transportPdu.size >= 6 && (it.transportPdu[0].toUByte().toInt() and 0x80) == 0
        }?.run {
            val akf = (transportPdu[0].toUByte().toInt() and 0b01000000) != 0
            val aid = if (akf) {
                (transportPdu[0].toUByte().toInt() and 0x3F).toUByte()
            } else null
            AccessMessage(
                source = source,
                destination = destination,
                networkKey = key,
                ivIndex = ivIndex,
                upperTransportPdu = transportPdu.copyOfRange(1, transportPdu.size),
                // TransMIC is always 32-bits for unsegmented messages.
                transportMicSize = 4.toUByte(),
                sequence = sequence,
                aid = aid
            )
        }

        /**
         * Creates an Access Message from the given list of Segmented Access Messages
         *
         * @param segments List of segmented access messages.
         * @return AccessMessage or null if the segments are invalid.
         */
        fun init(segments: List<SegmentedAccessMessage>): AccessMessage {
            val segment = segments.first()
            return AccessMessage(
                source = segment.source,
                destination = segment.destination,
                networkKey = segment.networkKey,
                ivIndex = segment.ivIndex,
                upperTransportPdu = segments.reduce { acc, seg ->
                    acc.copy(upperTransportPdu = acc.upperTransportPdu + seg.upperTransportPdu)
                }.upperTransportPdu,
                transportMicSize = segment.transportMicSize,
                sequence = segment.sequence,
                aid = segment.aid
            )
        }
    }
}
