@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportPdu
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKeyDerivatives
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import kotlin.experimental.and

/**
 * Defines a Network PDU received/sent by a node.
 *
 * @property pdu              Raw PDU data.
 * @property key              Network key used to decode/encode the PDU.
 * @property ivIndex          IV Index used to decode/encode the PDU.
 * @property type             PDU type.
 * @property ttl              Time to live.
 * @property sequence         Sequence number of the message.
 * @property source           Source address of the message.
 * @property destination      Destination address of the message.
 * @property transportPdu     Transport protocol data unit that's guaranteed to have 1 to 16 bytes.
 * @property ivi              Raw data of the upper transport layer PDU.
 * @property nid              Flag indicating if the message is a control message.
 * @property isSegmented      Flag indicating if the message is segmented.
 * @property messageSequence  Message sequence number.
 * @constructor Creates a Network PDU.
 */
internal data class NetworkPdu internal constructor(
    val pdu: ByteArray,
    val key: NetworkKey,
    val ivIndex: UInt,
    val ivi : UByte,
    val nid : UByte,
    val type: LowerTransportPduType,
    val ttl: UByte,
    val sequence: UInt,
    val source: MeshAddress,
    val destination: MeshAddress,
    val transportPdu: ByteArray
) {

    val isSegmented: Boolean
        get() = transportPdu[0] and 0x80.toByte() > 1

    val messageSequence: UInt
        get() = if (isSegmented) {
            val sequenceZero = ((transportPdu[1] and 0x7F).toInt() shl 6).toUShort() or
                    (transportPdu[2].toUInt() shr 2).toUShort()
            if (sequence and 0x1FFFFu < sequenceZero) {
                (sequence and 0xFFE000u) + sequenceZero.toUInt() - (0x1FFF + 1).toUInt()
            } else {
                (sequence and 0xFFE000u) + sequenceZero.toUInt()
            }
        } else sequence

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkPdu

        if (!pdu.contentEquals(other.pdu)) return false
        if (key != other.key) return false
        if (ivIndex != other.ivIndex) return false
        if (type != other.type) return false
        if (ttl != other.ttl) return false
        if (sequence != other.sequence) return false
        if (source != other.source) return false
        if (destination != other.destination) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false
        if (ivi != other.ivi) return false
        if (nid != other.nid) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pdu.contentHashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + ttl.hashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + transportPdu.contentHashCode()
        result = 31 * result + ivi.hashCode()
        result = 31 * result + nid.hashCode()
        return result
    }
}

/**
 * Helper object for encoding and decoding Network PDUs
 */
internal object NetworkPduDecoder {

    /**
     * Creates a Network PDU object from the received PDU. The initiator tries to de-obfuscate and
     * decrypt the data using given Network Key and IV Index.
     *
     * @param pdu           PDU received from the mesh network
     * @param pduType       Type of the PDU. This could be either a Network or a Proxy Configuration
     *                      PDU.
     * @param meshNetwork   Mesh network.
     * @return Network PDU or null if unable to decode the PDU.
     */
    fun decode(pdu: ByteArray, pduType: PduType, meshNetwork: MeshNetwork): NetworkPdu? {
        require(pduType == PduType.NETWORK_PDU || pduType == PduType.PROXY_CONFIGURATION) {
            return null
        }
        require(pdu.size >= 14) {
            return null
        }
        for (networkKey in meshNetwork.networkKeys) {
            decode(pdu, pduType, networkKey, meshNetwork.ivIndex)?.let {
                return it
            }
        }
        return null
    }

    /**
     * Creates a Network PDU object from the received PDU. The initiator tries to de-obfuscate and
     * decrypt the data using given Network Key and IV Index.
     *
     * @param pdu           PDU received from the mesh network
     * @param pduType       Type of the PDU. This could be either a Network or a Proxy Configuration
     *                      PDU.
     * @param networkKey    Network key to be used to decrypt the PDU.
     * @param ivIndex       IV Index of the mesh network.
     * @return Network PDU or null if unable to decode the PDU.
     */
    private fun decode(
        pdu: ByteArray,
        pduType: PduType,
        networkKey: NetworkKey,
        ivIndex: IvIndex
    ): NetworkPdu? {
        // The first byte is not obfuscated.
        val ivi: UByte = ((pdu[0].toUByte().toInt()) ushr 7).toUByte()
        val nid: UByte = ((pdu[0].toUByte().toInt()) and 0x7F).toUByte()
        val keySets = mutableListOf<NetworkKeyDerivatives>()
        // The NID must match.
        // If the Key Refresh procedure is in place, the received packet might have been encrypted
        // using an old key. We have to try both.
        networkKey.derivatives.takeIf { nid == it.nid }?.let { keySets += it }
        networkKey.oldDerivatives?.takeIf { nid == it.nid }?.let { keySets += it }
        require(keySets.isNotEmpty()) { return null }

        val currentIvIndex = ivIndex.index(ivi)

        for (keys in keySets) {
            // 6 bytes following IVI
            val obfuscatedData = pdu.copyOfRange(fromIndex = 1, toIndex = 7)
            // 7 bytes of encrypted data
            val random = pdu.copyOfRange(fromIndex = 7, toIndex = 14)

            val deobfuscatedData = Crypto.obfuscate(
                data = obfuscatedData,
                random = random,
                ivIndex = currentIvIndex,
                privacyKey = keys.privacyKey
            )

            // First validation: Control messages have a NetMIC of size 64 bits.
            val ctl = ((deobfuscatedData[0].toUByte().toInt()) shr 7).toUByte()
            if (ctl.toInt() != 1 && pdu.size < 18) continue

            val type = LowerTransportPduType.from(ctl)!!
            val ttl = (deobfuscatedData[0].toUByte().toInt() and 0x7F).toUByte()

            // Multiple octet values use Big Endian.
            val sequence = (((deobfuscatedData[1].toUByte().toInt()) shl 16) or
                    ((deobfuscatedData[2].toUByte().toInt()) shl 8) or
                    deobfuscatedData[3].toUByte().toInt()).toUInt()

            val src = (((deobfuscatedData[4].toUByte().toInt()) shl 8) or
                    deobfuscatedData[5].toUByte().toInt())

            val micOffset = pdu.size - type.netMicSize
            val destAndTransportPdu = pdu.copyOfRange(fromIndex = 7, toIndex = pdu.size)
            val mic = pdu.copyOfRange(fromIndex = micOffset, toIndex = pdu.size)

            val nonce = byteArrayOf(pduType.nonceId.toByte()) +
                    deobfuscatedData +
                    byteArrayOf(0x00, 0x00) +
                    currentIvIndex.toByteArray()

            // Pad
            if (pduType == PduType.PROXY_CONFIGURATION) nonce[1] = 0x00

            try {
                val decryptedData = Crypto.decrypt(
                    data = destAndTransportPdu,
                    key = keys.encryptionKey,
                    nonce = nonce,
                    micSize = mic.size
                ) ?: continue

                return NetworkPdu(
                    pdu = pdu,
                    key = networkKey,
                    ivIndex = currentIvIndex,
                    ivi = nid,
                    nid = nid,
                    type = type,
                    ttl = ttl,
                    sequence = sequence,
                    source = MeshAddress.create(address = src.toUShort()),
                    destination = MeshAddress.create(
                        address = decryptedData[0].toInt() shl 8 or decryptedData[1].toInt()
                    ),
                    decryptedData.copyOfRange(fromIndex = 2, toIndex = decryptedData.size)
                )
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    /**
     * Creates the Network PDU. This method encrypts and deobfuscates data that are to be send to
     * the mesh network.
     *
     * @param lowerTransportPdu  Network pdu to be decoded.
     * @param pduType            Pdu type.
     * @param sequence           Sequence number of the pdu.
     * @param ttl                TTL of the pdu.
     * @return De-obfuscated and decoded the Network Pdu, or null if the PDU was not signed with any
     *         of the Network Keys, the IV Index was not valid or the PDU was invalid.
     */
    fun encode(
        lowerTransportPdu: LowerTransportPdu,
        pduType: PduType,
        sequence: UInt,
        ttl: UByte
    ): NetworkPdu {
        require(pduType == PduType.NETWORK_PDU || pduType == PduType.PROXY_CONFIGURATION) {
            throw IllegalArgumentException(
                "Only ${PduType.NETWORK_PDU} and ${PduType.PROXY_CONFIGURATION} can be encoded " +
                        "into a NetworkPdu."
            )
        }
        val networkKey = lowerTransportPdu.networkKey
        val keys = networkKey.transmitKeys
        val ivIndex = lowerTransportPdu.ivIndex
        val ivi = (ivIndex and 0x1u).toUByte()
        val nid = keys.nid
        val type = lowerTransportPdu.type
        val source = lowerTransportPdu.source.address
        val destination = lowerTransportPdu.destination.address
        val transportPdu = lowerTransportPdu.transportPdu

        val iviNid = ((ivi.toInt() shl 7) or (nid.toInt() and 0x7F)).toByte()
        val ctlTtl = ((type.rawValue.toInt() shl 7) or (ttl.toInt() and 0x7F)).toByte()

        val seq = sequence.toByteArray().let { it.copyOfRange(fromIndex = 1, toIndex = it.size) }
        val deobfuscatedData = byteArrayOf(ctlTtl) + seq + source.toByteArray()
        val decryptedData = destination.toByteArray() + transportPdu

        val nonce = byteArrayOf(pduType.nonceId.toByte()) +
                deobfuscatedData +
                byteArrayOf(0x00, 0x00) +
                ivIndex.toByteArray()
        // Pad
        if (pduType == PduType.PROXY_CONFIGURATION) nonce[1] = 0x00

        val encryptedData = Crypto.encrypt(
            data = decryptedData,
            key = keys.encryptionKey,
            nonce = nonce,
            micSize = type.netMicSize
        )
        val obfuscatedData = Crypto.obfuscate(
            data = deobfuscatedData,
            random = encryptedData,
            ivIndex = ivIndex,
            privacyKey = keys.privacyKey
        )
        val pdu = byteArrayOf(iviNid) + obfuscatedData + encryptedData
        return NetworkPdu(
            pdu = pdu,
            key = networkKey,
            ivIndex = ivIndex,
            ivi = ivi,
            nid = nid,
            type = type,
            ttl = ttl,
            sequence = sequence,
            source = lowerTransportPdu.source,
            destination = lowerTransportPdu.destination,
            transportPdu = transportPdu
        )
    }
}