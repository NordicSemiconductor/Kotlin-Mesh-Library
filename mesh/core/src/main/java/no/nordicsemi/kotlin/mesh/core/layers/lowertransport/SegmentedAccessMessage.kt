@file:Suppress("unused")
@file:OptIn(ExperimentalStdlibApi::class)

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.UpperTransportPdu
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import kotlin.experimental.or
import kotlin.math.min

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
    val aid: Byte? = null,
    val sequence: UInt,
    val transportMicSize: UByte,
) : SegmentedMessage {

    override val transportPdu: ByteArray
        get() {
            var octet0 = 0x80.toByte() // SEG = 1
            aid?.let { aid ->
                octet0 = octet0 or 0b01000000.toByte() // AKF = 1
                octet0 = octet0 or aid
            }
            val octet1 = ((transportMicSize.toInt() shl 3) and 0x80).toByte() or
                    (sequenceZero.toInt() shr 6).toByte()
            val octet2 = ((sequenceZero and 0x3Fu).toInt() shl 2).toByte() or
                    (segmentOffset.toInt() shr 3).toByte()
            val octet3 = ((segmentOffset and 0x07u).toInt() shl 5).toByte() or
                    (lastSegmentNumber and 0x1Fu).toByte()
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

    internal companion object {

        /**
         * Creates a SegmentedAccessMessage from a given Network PDU.
         *
         * @param networkPdu Network pdu to be decoded.
         * @return SegmentedAccessMessage or null otherwise.
         */
        fun init(networkPdu: NetworkPdu): SegmentedAccessMessage? = networkPdu.run {
            require(
                transportPdu.size >= 5
                        && transportPdu[0].toInt() and 0x80 != 0x00
            ) { return null }

            val akf = transportPdu[0].toInt() shr 7 and 1
            val aid: Byte? = when (akf == 1) {
                true -> (transportPdu[0].toInt() and 0x3F).toByte()
                false -> null
            }
            val szmic = transportPdu[1].toInt() shr 7 and 1
            val transportMicSize = when (szmic == 0) {
                true -> 4
                false -> 8
            }.toUByte()

            val sequenceZero = ((transportPdu[1].toInt() and 0x7F) shl 6).toUShort() or
                    (transportPdu[2].toInt() ushr 2).toUShort()
            val segmentOffset = (((transportPdu[2].toInt() and 0x03) shl 3) or
                    ((transportPdu[3].toInt() and 0xE0) shr 5)).toUByte()
            val lastSegmentNumber = (transportPdu[3].toInt() and 0x1F).toUByte()

            require(segmentOffset <= lastSegmentNumber) { return null }

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

        /**
         * Creates a SegmentedAccessMessage from the given Upper Transport PDU, network key and
         * offset.
         *
         * @param pdu        Upper transport PDU.
         * @param networkKey Network key to be used to encrypt the message.
         * @param offset     Offset of the segment.
         * @return SegmentedAccessMessage
         */
        fun init(
            pdu: UpperTransportPdu,
            networkKey: NetworkKey,
            offset: UByte
        ): SegmentedAccessMessage {
            val lowerBound = offset.toInt() * 12
            val upperBound = min(pdu.transportPdu.size, (offset.toInt() + 1) * 12)
            val segment = pdu.transportPdu.sliceArray(lowerBound until upperBound)
            return SegmentedAccessMessage(
                source = MeshAddress.create(pdu.source),
                destination = pdu.destination,
                networkKey = networkKey,
                ivIndex = pdu.ivIndex,
                upperTransportPdu = segment,
                sequence = pdu.sequence,
                sequenceZero = (pdu.sequence and 0x1FFFu).toUShort(),
                segmentOffset = offset,
                lastSegmentNumber = (((pdu.transportPdu.size + 11).toUByte() / 12u) - 1u).toUByte(),
                aid = pdu.aid,
                transportMicSize = pdu.transportMicSize
            )
        }
    }
}
