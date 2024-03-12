@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.data.hasBitCleared
import no.nordicsemi.kotlin.data.hasBitSet
import no.nordicsemi.kotlin.data.shl
import no.nordicsemi.kotlin.data.shr
import no.nordicsemi.kotlin.mesh.core.exception.InvalidPdu
import no.nordicsemi.kotlin.mesh.core.layers.network.NetworkPdu
import no.nordicsemi.kotlin.mesh.core.layers.uppertransport.UpperTransportPdu
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

/**
 * Data class defining a Segmented Access Message.
 *
 * @property aid                Application Key identifier.
 * @property sequence           Sequence number of the message.
 * @property transportMicSize   Size of the transport MIC which is 4 or 8 bytes.
 */
internal class SegmentedAccessMessage(
    // Access Message
    override val source: MeshAddress,
    override val destination: MeshAddress,
    override val networkKey: NetworkKey,
    override val ivIndex: UInt,
    override val upperTransportPdu: ByteArray,
    override val transportMicSize: UByte,
    override val sequence: UInt,
    override val aid: Byte? = null,
    // Segmented Message
    override val message: MeshMessage? = null,
    override val userInitialized: Boolean = false,
    override val sequenceZero: UShort,
    override val segmentOffset: UByte,
    override val lastSegmentNumber: UByte,
) : AccessMessage(source, destination, networkKey, ivIndex, upperTransportPdu, transportMicSize, sequence), SegmentedMessage {

    override val transportPdu: ByteArray
        get() {
            var octet0 = 0x80.toByte() // SEG = 1
            aid?.let { aid ->
                octet0 = octet0 or 0b01000000 // AKF = 1
                octet0 = octet0 or aid
            }
            val octet1 = (transportMicSize and 0x08u shl 4).toByte() or // 8 -> 0x80, 4 -> 0x00
                         (sequenceZero shr 6).toByte()
            val octet2 = (sequenceZero shl 2).toByte() or
                         (segmentOffset shr 3).toByte()
            val octet3 = (segmentOffset and 0x07u shl 5).toByte() or
                         (lastSegmentNumber and 0x1Fu).toByte()
            return byteArrayOf(octet0, octet1, octet2, octet3) + upperTransportPdu
        }

    override val type = LowerTransportPduType.ACCESS_MESSAGE

    internal companion object {

        /**
         * Creates a SegmentedAccessMessage from a given Network PDU.
         *
         * @param networkPdu Network pdu to be decoded.
         * @return SegmentedAccessMessage or null otherwise.
         */
        fun init(pdu: NetworkPdu): SegmentedAccessMessage {
            // Minimum length of a Access Message is 6 bytes:
            // * 1 byte for SEG | AKF | AID
            // * 3 bytes for SZMIC | SeqZero | SegO | SegN
            // * At least 1 byte of segment payload
            require(pdu.transportPdu.size >= 5) { throw InvalidPdu }

            // Make sure the SEG is 0, that is the message is segmented.
            require(pdu.transportPdu[0] hasBitSet 7) { throw InvalidPdu } // TODO Change exception?

            val akf = pdu.transportPdu[0] hasBitSet 6
            val aid = if (akf) pdu.transportPdu[0] and 0x3F else null

            val szmic = pdu.transportPdu[1] hasBitSet 7
            val transportMicSize: UByte = if (szmic) 8u else 4u
            val sequenceZero = (pdu.transportPdu[1].toUShort() and 0x7Fu shl 6) or
                               (pdu.transportPdu[2].toUShort() shr 2)
            val segmentOffset = (pdu.transportPdu[2].toUByte() and 0x03u shl 3) or
                                (pdu.transportPdu[3].toUByte() shr 5)
            val lastSegmentNumber = pdu.transportPdu[3].toUByte() and 0x1Fu

            // Make sure SegO is less than or equal to SegN.
            require(segmentOffset <= lastSegmentNumber) { throw InvalidPdu } // TODO Change exception?

            val upperTransportPdu = pdu.transportPdu.copyOfRange(4, pdu.transportPdu.size)
            val sequence = (pdu.sequence and 0xFFE000u) or sequenceZero.toUInt()
            return SegmentedAccessMessage(
                source = pdu.source,
                destination = pdu.destination,
                networkKey = pdu.key,
                ivIndex = pdu.ivIndex,
                upperTransportPdu = upperTransportPdu,
                transportMicSize = transportMicSize,
                sequence = sequence,
                aid = aid,
                sequenceZero = sequenceZero,
                segmentOffset = segmentOffset,
                lastSegmentNumber = lastSegmentNumber,
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
            val segment = pdu.transportPdu.copyOfRange(lowerBound, upperBound)
            return SegmentedAccessMessage(
                source = MeshAddress.create(pdu.source),
                destination = pdu.destination,
                networkKey = networkKey,
                ivIndex = pdu.ivIndex,
                upperTransportPdu = segment,
                transportMicSize = pdu.transportMicSize,
                sequence = pdu.sequence,
                aid = pdu.aid,
                userInitialized = pdu.userInitiated,
                sequenceZero = (pdu.sequence and 0x1FFFu).toUShort(),
                segmentOffset = offset,
                lastSegmentNumber = (((pdu.transportPdu.size + 11) / 12) - 1).toUByte(),
            )
        }
    }
}
