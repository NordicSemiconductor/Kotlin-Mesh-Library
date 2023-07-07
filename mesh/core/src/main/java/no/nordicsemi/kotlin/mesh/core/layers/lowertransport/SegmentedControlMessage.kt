@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.layers.network.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import kotlin.experimental.and

/**
 * Data class defining a Segmented Control Message.
 *
 * @property opCode             Message Op Code.
 * @property ttl                TTL value of the message.
 */
internal data class SegmentedControlMessage(
    val opCode: UByte,
    val ttl: UByte,
    val localElement: Element? = null,
    override val source: MeshAddress,
    override val destination: MeshAddress,
    override val networkKey: NetworkKey,
    override val ivIndex: UInt,
    override val upperTransportPdu: ByteArray,
    override val message: MeshMessage? = null,
    override val userInitialized: Boolean = false,
    override val sequenceZero: UShort,
    override val segmentOffset: UByte,
    override val lastSegmentNumber: UByte
) : SegmentedMessage {

    override val transportPdu: ByteArray
        get() {
            val octet0 = 0x80.toUByte() or (opCode and 0x7Fu)
            val octet1 = (sequenceZero.toInt() shr 5).toUByte()
            val octet2 = ((sequenceZero.toInt() and 0x3F) shl 2).toUByte() or (segmentOffset and 0x07u)
            val octet3 = (segmentOffset and 0x07u) or (lastSegmentNumber and 0x07u)
            return byteArrayOf(octet0.toByte(), octet1.toByte(), octet2.toByte(), octet3.toByte()) +
                    upperTransportPdu
        }

    override val type = LowerTransportPduType.CONTROL_MESSAGE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SegmentedControlMessage

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (networkKey != other.networkKey) return false
        if (ivIndex != other.ivIndex) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false
        if (!upperTransportPdu.contentEquals(other.upperTransportPdu)) return false
        if (message != other.message) return false
        if (userInitialized != other.userInitialized) return false
        if (sequenceZero != other.sequenceZero) return false
        if (segmentOffset != other.segmentOffset) return false
        if (lastSegmentNumber != other.lastSegmentNumber) return false
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
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + userInitialized.hashCode()
        result = 31 * result + sequenceZero.hashCode()
        result = 31 * result + segmentOffset.hashCode()
        result = 31 * result + lastSegmentNumber.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }

    override fun toString() = "Segmented $type (opCode :$opCode, seqZero: $sequenceZero, seg0 " +
            "$segmentOffset, segN: $lastSegmentNumber, " +
            "data: ${upperTransportPdu.encodeHex(true)})"

    internal object Decoder {

        /**
         * Decodes a given NetworkPdu containing a segmented control message.
         *
         * @param networkPdu Network pdu containing the segmented control message.
         * @return SegmentedControlMessage containing the decoded data.
         */
        fun decode(networkPdu: NetworkPdu): SegmentedControlMessage? = networkPdu.run {
            val opCode: UByte
            val sequenceZero: UShort
            val segmentOffset: UByte
            val lastSegmentNumber: UByte
            transportPdu.let { data ->
                require(data.size >= 5 && data[0] and 0x80.toByte() != 0x00.toByte()) {
                    return null
                }
                opCode = (data[0].toInt() and 0x7F).toUByte()
                require(opCode != 0x00.toUByte()) { return null }
                sequenceZero = (data[1].toInt() and 0x7F shl 6).toUShort() or
                        (data[2].toInt() shr 2).toUShort()
                segmentOffset = (data[2].toInt() and 0x03 shl 3 or
                        (data[3].toInt() shr 5)).toUByte()
                lastSegmentNumber = (data[3].toInt() and 0x1F).toUByte()

                require(segmentOffset <= lastSegmentNumber) { return null }
            }

            SegmentedControlMessage(
                opCode = opCode,
                ttl = ttl,
                source = src,
                destination = dst,
                networkKey = key,
                ivIndex = ivIndex,
                upperTransportPdu = transportPdu.drop(4).toByteArray(),
                sequenceZero = sequenceZero,
                segmentOffset = segmentOffset,
                lastSegmentNumber = lastSegmentNumber
            )
        }
    }
}
