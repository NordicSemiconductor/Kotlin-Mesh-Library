@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import kotlin.experimental.and
import kotlin.experimental.or

/**
 * Data class defining a Segmented Access Message.
 *
 * @property aid                Application Key identifier.
 * @property sequence           Sequence number of the message.
 * @property transportMicSize   Size of the transport MIC which is 4 or 8 bytes.
 */
internal data class SegmentedAccessMessage(
    override val source: MeshAddress,
    override val destination: MeshAddress,
    override val networkKey: NetworkKey,
    override val ivIndex: UInt,
    override val upperTransportPdu: ByteArray,
    override val message: MeshMessage? = null,
    override val userInitialized: Boolean = false,
    override val sequenceZero: UShort,
    override val segmentOffset: UByte,
    override val lastSegmentNumber: UByte,
    val aid: UByte? = null,
    val sequence: UInt,
    val transportMicSize: UByte,
) : SegmentedMessage {

    override val transportPdu: ByteArray
        get() {
            var octet0 = 0x80.toByte() // SEG = 1
            aid?.let { aid ->
                octet0 = octet0 or 0b01000000.toByte() // AKF = 1
                octet0 = octet0 or aid.toByte()
            }
            val octet1 = ((transportMicSize.toInt() shl 3) and 0x80).toByte() or
                    (sequenceZero.toInt() shr 6).toByte()
            val octet2 = ((sequenceZero and 0x3Fu).toInt() shl 2).toByte() or
                    (segmentOffset.toInt() shr 3).toByte()
            val octet3 = ((segmentOffset and 0x07.toUByte()).toInt() shl 5).toByte() or
                    (lastSegmentNumber.toInt() and 0x1F).toByte()
            return byteArrayOf(octet0, octet1, octet2, octet3) + upperTransportPdu
        }

    override val type = LowerTransportPduType.ACCESS_MESSAGE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentedAccessMessage

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (networkKey != other.networkKey) return false
        if (ivIndex != other.ivIndex) return false
        if (type != other.type) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false
        if (!upperTransportPdu.contentEquals(other.upperTransportPdu)) return false
        if (message != other.message) return false
        if (userInitialized != other.userInitialized) return false
        if (sequenceZero != other.sequenceZero) return false
        if (segmentOffset != other.segmentOffset) return false
        if (lastSegmentNumber != other.lastSegmentNumber) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + networkKey.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + transportPdu.contentHashCode()
        result = 31 * result + upperTransportPdu.contentHashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + userInitialized.hashCode()
        result = 31 * result + sequenceZero.hashCode()
        result = 31 * result + segmentOffset.hashCode()
        result = 31 * result + lastSegmentNumber.hashCode()
        return result
    }

    companion object Decoder {

        /**
         * Decodes the given [NetworkPdu] to a [SegmentedAccessMessage].
         *
         * @param networkPdu Network pdu to be decoded.
         * @return SegmentedAccessMessage or null otherwise.
         */
        @Suppress("MoveVariableDeclarationIntoWhen")
        fun decode(networkPdu: NetworkPdu): SegmentedAccessMessage? = networkPdu.run {
            require(
                transportPdu.size >= 5 && transportPdu[0] and
                        0x80.toByte() != 0x00.toByte()
            ) { return null }

            val akf = transportPdu[0] and 0x0b01000000.toByte() != 0x00.toByte()
            val aid: UByte? = when (akf) {
                true -> (transportPdu[0] and 0x3F.toByte()).toUByte()
                false -> null
            }
            val szmic = (transportPdu[1].toInt() shr 7).toUByte()
            val transportMicSize = when (szmic == 0x00.toUByte()) {
                true -> 4
                false -> 8
            }.toUByte()

            val sequenceZero = ((transportPdu[1] and 0x7F.toByte()).toInt() shl 6).toUShort() or
                    (transportPdu[2].toInt() shr 2).toUShort()
            val segmentOffset = ((transportPdu[2] and 0x03.toByte()).toInt() shl 3 or
                    ((transportPdu[3] and 0xE0.toByte()).toInt() shr 5)).toUByte()
            val lastSegmentNumber = (transportPdu[3] and 0x1F.toByte()).toUByte()

            require(segmentOffset <= lastSegmentNumber) {
                return null
            }

            val upperTransportPdu = transportPdu.drop(4).toByteArray()
            val sequence = (this.sequence and 0xFFE000u) or sequenceZero.toUInt()
            return SegmentedAccessMessage(
                source = source,
                destination = destination,
                networkKey = key,
                ivIndex = ivIndex,
                upperTransportPdu = upperTransportPdu,
                sequence = sequence,
                sequenceZero = sequenceZero,
                segmentOffset = segmentOffset,
                lastSegmentNumber = lastSegmentNumber,
                aid = aid,
                transportMicSize = transportMicSize
            )
        }
    }
}
