package no.nordicsemi.kotlin.mesh.core.layers.uppertransport

import no.nordicsemi.kotlin.mesh.core.layers.AccessKeySet
import no.nordicsemi.kotlin.mesh.core.layers.DeviceKeySet
import no.nordicsemi.kotlin.mesh.core.layers.KeySet
import no.nordicsemi.kotlin.mesh.core.layers.access.AccessPdu
import no.nordicsemi.kotlin.mesh.core.layers.lowertransport.AccessMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessage
import no.nordicsemi.kotlin.mesh.core.messages.MeshMessageSecurity
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import no.nordicsemi.kotlin.mesh.core.model.boundTo
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

/**
 * UpperTransportPdu defines the credentials used to encrypt a message.
 *
 * @property source           Source address of the message.
 * @property destination      Destination address of the message.
 * @property aid              6-bit Application key identifier, or 'nil' if a Device Key was used.
 * @property transportMicSize Size of the transport MIC which is 4 or 8 bytes.
 * @property transportPdu     Raw data of the lower transport layer PDU.
 * @property accessPdu        Raw data of the upper transport layer PDU.
 * @property sequence         Sequence number used to encode this message.
 * @property ivIndex          IV Index used to encode this message.
 * @property message          Mesh message that was sent or null if the message was received.
 * @property userInitiated    Flag indicating whether the message was user initiated.
 * @constructor Creates an UpperTransportPdu.
 */
internal data class UpperTransportPdu(
    val source: Address,
    val destination: MeshAddress,
    val aid: Byte?,
    val transportMicSize: UByte,
    val transportPdu: ByteArray,
    val accessPdu: ByteArray,
    val sequence: UInt,
    val ivIndex: UInt,
    val message: MeshMessage?,
    val userInitiated: Boolean
) {

    override fun toString(): String {
        val micSize = transportMicSize.toInt()
        val encryptedDataSize = transportPdu.size - micSize
        val encryptedData = transportPdu.sliceArray(
            0 until encryptedDataSize
        )
        val mic = transportPdu.sliceArray(
            encryptedDataSize until encryptedDataSize + micSize
        )
        return "Upper transport PDU (encrypted data: ${encryptedData.encodeHex(true)}, " +
                "transMIC: ${mic.encodeHex(true)}"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpperTransportPdu

        if (source != other.source) return false
        if (destination != other.destination) return false
        if (aid != other.aid) return false
        if (transportMicSize != other.transportMicSize) return false
        if (!transportPdu.contentEquals(other.transportPdu)) return false
        if (!accessPdu.contentEquals(other.accessPdu)) return false
        if (sequence != other.sequence) return false
        if (ivIndex != other.ivIndex) return false
        if (message != other.message) return false
        if (userInitiated != other.userInitiated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = source.hashCode()
        result = 31 * result + destination.hashCode()
        result = 31 * result + (aid?.hashCode() ?: 0)
        result = 31 * result + transportMicSize.hashCode()
        result = 31 * result + transportPdu.contentHashCode()
        result = 31 * result + accessPdu.contentHashCode()
        result = 31 * result + sequence.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + userInitiated.hashCode()
        return result
    }

    internal companion object {

        /**
         * Constructs an UpperTransportPdu from a given AccessMessage.
         *
         * @param message       AccessMessage to be decode from.
         * @param key           Key to be used for decryption.
         * @param virtualGroup  Virtual group address if the message is a virtual group message.
         * @return an UpperTransportPdu or null if the pdu could not be decoded.
         */
        fun init(message: AccessMessage, key: ByteArray, virtualGroup: Group?): UpperTransportPdu? {
            val micSize = message.transportMicSize.toInt()
            val encryptedData = message.upperTransportPdu.copyOfRange(
                fromIndex = 0, toIndex = message.upperTransportPdu.size
            )
            /*val mic = accessMessage.upperTransportPdu.sliceArray(
                encryptedDataSize until encryptedDataSize + micSize
            )*/

            // The nonce type is 0x01 for messages signed with Application Key and 0x02 for messages
            // signed with Device Key (Configuration Messages).
            val type = message.aid?.let { 0x01 } ?: 0x02
            // ASZMIC is set to 1 for messages sent with high security(64-bit TransMIC). This is
            // allowed only for Segmented Access Messages.
            val aszmic = if (micSize == 4) 0 else 1
            val seq = message.sequence.toByteArray().let {
                it.copyOfRange(fromIndex = 1, toIndex = it.size)
            }

            val nonce = byteArrayOf(type.toByte(), ((aszmic shl 7).toByte())) + seq +
                    message.source.address.toByteArray() +
                    message.destination.address.toByteArray() +
                    message.ivIndex.toByteArray()

            return Crypto.decrypt(
                data = encryptedData,
                key = key,
                nonce = nonce,
                additionalData = null,
                micSize = micSize
            )?.let { decryptedData ->
                UpperTransportPdu(
                    source = message.source.address,
                    destination = virtualGroup?.address?.address?.let { address ->
                        MeshAddress.create(address)
                    } ?: message.destination,
                    aid = message.aid,
                    transportMicSize = message.transportMicSize,
                    transportPdu = message.upperTransportPdu,
                    accessPdu = decryptedData,
                    sequence = message.sequence,
                    ivIndex = message.ivIndex,
                    message = null,
                    userInitiated = false
                )
            }
        }

        /**
         * Constructs an UpperTransportPdu from a given AccessPdu.
         *
         * @param pdu           AccessPdu to be decode from.
         * @param keySet        KeySet to be used for encryption.
         * @param sequence      Sequence number of the message.
         * @param ivIndex       Current IV Index.
         * @return              an UpperTransportPdu.
         */
        fun init(
            pdu: AccessPdu,
            keySet: KeySet,
            sequence: UInt,
            ivIndex: IvIndex
        ): UpperTransportPdu {
            val security = pdu.message!!.security
            // The nonce type is 0x01 for messages signed with Application Key and 0x02 for messages
            // signed using Device Key (Configuration Messages).
            val type = if (keySet.aid != null) 0x01 else 0x02
            // ASZMIC is set to 1 for messages that shall be sent with high security
            // (64-bit TransMIC). This is possible only for Segmented Access Messages.
            val aszmic = if ((security == MeshMessageSecurity.High) &&
                (pdu.accessPdu.size > 11 || pdu.isSegmented)
            ) 1 else 0

            val seq = sequence.toByteArray().let {
                it.copyOfRange(fromIndex = 1, toIndex = it.size)
            }

            val nonce = byteArrayOf(type.toByte(), ((aszmic shl 7).toByte())) + seq +
                    pdu.source.toByteArray() +
                    pdu.destination.address.toByteArray() +
                    ivIndex.index.toByteArray()
            val transportMicSize = if (aszmic == 0) 4 else 8

            return UpperTransportPdu(
                source = pdu.source,
                destination = pdu.destination,
                aid = keySet.aid,
                transportMicSize = transportMicSize.toUByte(),
                transportPdu = Crypto.encrypt(
                    data = pdu.accessPdu,
                    key = keySet.accessKey,
                    nonce = nonce,
                    additionalData = (pdu.destination as? VirtualAddress)?.uuid?.toByteArray(),
                    micSize = transportMicSize
                ),
                accessPdu = pdu.accessPdu,
                sequence = sequence,
                ivIndex = ivIndex.transmitIvIndex,
                message = pdu.message,
                userInitiated = pdu.userInitiated
            )
        }

        /**
         * Decodes the Access Message using a matching Application Key based on the 'aid' field
         * value, or the Device Key of hte local or source Node.
         *
         * @param message AccessMessage to be decode from.
         * @param network Network to be used for encryption.
         * @return A pair containing the UpperTransportPdu and the KeySet used to encrypt the
         *         message or null if the pdu could not be decoded.
         */
        fun decode(
            message: AccessMessage,
            network: MeshNetwork
        ): Pair<UpperTransportPdu, KeySet>? {
            // Was the message signed using Application Key?
            message.aid?.let { aid ->
                // When the message was sent to a Virtual Address, the message must be decoded with the
                // Virtual Label as Additional Data.
                val matchingGroups = if (message.destination is VirtualAddress) {
                    network.groups.filter { group ->
                        group.address == message.destination
                    }.toMutableList()
                } else emptyList()

                // Go through all the application keys bound to the network key that the message was
                // decoded with.
                for (applicationKey in network.applicationKeys.boundTo(message.networkKey)) {
                    // The matchingGroups contains either a list of Virtual Groups, or a single nil
                    for (group in matchingGroups) {
                        // Each time try decoding using the new, or the old key (if such exist) when the
                        // generated aid matches the one sent int he message.
                        if (aid == applicationKey.aid) {
                            init(
                                message = message,
                                key = applicationKey.key,
                                virtualGroup = group
                            )?.let {
                                return Pair(
                                    it,
                                    AccessKeySet(applicationKey = applicationKey)
                                )
                            }
                        }
                        val oldAid = requireNotNull(applicationKey.oldAid) { return null }
                        require(aid == oldAid) { return null }
                        val key = requireNotNull(applicationKey.oldKey) { return null }
                        return init(
                            message = message,
                            key = key,
                            virtualGroup = group
                        )?.let { pdu ->
                            Pair(pdu, AccessKeySet(applicationKey = applicationKey))
                        }
                    }
                }
            } ?: run {
                // Try decoding using source's Node Device Key. This should work if a status message
                // was sent as a response to a Config Message sent by this Provisioner.
                val node = network.node(message.source) ?: network.node(message.destination)
                // On the other hand, if another Provisioner is sending a Config Messages, they will
                // be signed using the target node Device Key instead.
                return node?.deviceKey?.let { deviceKey ->
                    init(message = message, key = deviceKey, virtualGroup = null)?.let {
                        Pair(it, DeviceKeySet.init(networkKey = message.networkKey, node = node)!!)
                    }
                }
            }
            return null
        }
    }
}