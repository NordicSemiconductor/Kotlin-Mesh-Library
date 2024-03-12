@file:Suppress("unused")
@file:OptIn(ExperimentalStdlibApi::class)

package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.data.hasBitSet
import no.nordicsemi.kotlin.data.shl
import no.nordicsemi.kotlin.data.shr
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.data.ushr
import no.nordicsemi.kotlin.mesh.bearer.PduType
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportPdu
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.LowerTransportPduType
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.NetworkKeyDerivatives
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import kotlin.experimental.and
import kotlin.experimental.or

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
internal class NetworkPdu internal constructor(
    val pdu: ByteArray,
    val key: NetworkKey,
    val ivIndex: UInt,
    val ivi: Byte,
    val nid: Byte,
    val type: LowerTransportPduType,
    val ttl: UByte,
    val sequence: UInt,
    val source: MeshAddress,
    val destination: MeshAddress,
    val transportPdu: ByteArray
) {
    val isSegmented: Boolean
        get() = transportPdu[0] hasBitSet 7

    val messageSequence: UInt
        get() = if (isSegmented) {
            val sequenceZero = (transportPdu[1].toUShort() and 0x7Fu shl 6) or
                               (transportPdu[2].toUShort() shr 2)
            if ((sequence and 0x1FFFFu) < sequenceZero) {
                (sequence and 0xFFE000u) + sequenceZero.toUInt() - (0x1FFF + 1).toUInt()
            } else {
                (sequence and 0xFFE000u) + sequenceZero.toUInt()
            }
        } else sequence

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString() =
        "NetworkPdu (ivi: $ivi, nid: ${nid.toHexString()}, ctl: ${type.rawValue}, " +
                "ttl: $ttl, seq: $sequence, src: ${source.toHexString()}, " +
                "dst: ${destination.toHexString()}, " +
                "transportPdu: 0x${pdu.copyOfRange(0, pdu.size - type.netMicSize).toHexString()}, " +
                "netMic: 0x${pdu.copyOfRange(pdu.size - type.netMicSize, pdu.size).toHexString()})"
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
        val ivi: Byte = pdu[0] ushr 7
        val nid: Byte = pdu[0] and 0x7F
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
            val ctl = deobfuscatedData[0] ushr 7
            val type = LowerTransportPduType.from(ctl)!!
            if (type != LowerTransportPduType.CONTROL_MESSAGE && pdu.size < 18) continue

            val ttl = (deobfuscatedData[0] and 0x7F).toUByte()

            // Multiple octet values use Big Endian.
            val sequence = (deobfuscatedData[1].toUInt() shl 16) or
                           (deobfuscatedData[2].toUInt() shl 8) or
                            deobfuscatedData[3].toUInt()

            val src = (deobfuscatedData[4].toUShort() shl 8) or
                       deobfuscatedData[5].toUShort()

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

                val dst = (decryptedData[0].toUShort() shl 8) or
                           decryptedData[1].toUShort()

                return NetworkPdu(
                    pdu = pdu,
                    key = networkKey,
                    ivIndex = currentIvIndex,
                    ivi = nid,
                    nid = nid,
                    type = type,
                    ttl = ttl,
                    sequence = sequence,
                    source = MeshAddress.create(address = src),
                    destination = MeshAddress.create(address = dst),
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
        val ivi = (ivIndex and 0x1u).toByte()
        val nid = keys.nid
        val type = lowerTransportPdu.type
        val source = lowerTransportPdu.source.address
        val destination = lowerTransportPdu.destination.address
        val transportPdu = lowerTransportPdu.transportPdu

        val iviNid = (ivi shl 7) or nid
        val ctlTtl = (type.rawValue shl 7) or ttl.toByte()

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