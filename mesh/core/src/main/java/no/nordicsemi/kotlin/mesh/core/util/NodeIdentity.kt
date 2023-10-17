@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.util

import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import java.util.UUID

/**
 * The Node Identity contains information from Node Identity or Private Node Identity beacon.
 *
 * This can be used to identify a node in the network.
 */
internal interface NodeIdentity {

    /**
     * Returns whether the identity matches given node.
     *
     * @param node Node to be matched.
     * @return true if the identity matches, false otherwise.
     */
    fun matches(node: Node): Boolean

    /**
     * Checks if the given hash is valid.
     *
     * @param hash          Hash to be checked.
     * @param data          Data to be hashed.
     * @param identityKey   Identity key.
     */
    fun isValidHash(hash: ByteArray, data: ByteArray, identityKey: ByteArray?) =
        identityKey?.let {
            hash.contentEquals(Crypto.calculateHash(data, identityKey = it))
        } ?: false
}

/**
 * Representation of Node Identity advertisement packet.
 *
 * @property hash    Function of the included random number and identity information.
 * @property random  64-bit random number.
 */
data class PublicNodeIdentity internal constructor(
    val hash: ByteArray,
    val random: ByteArray
) : NodeIdentity {
    override fun matches(node: Node): Boolean {
        val data = ByteArray(6) { 0 } + random + node.primaryUnicastAddress.address.toByteArray()
        for (key in node.networkKeys) {
            if (isValidHash(hash = hash, data = data, identityKey = key.derivatives.identityKey))
                return true

            if (isValidHash(
                    hash = hash,
                    data = data,
                    identityKey = key.oldDerivatives?.identityKey
                )
            ) return true
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicNodeIdentity

        if (!hash.contentEquals(other.hash)) return false
        if (!random.contentEquals(other.random)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + random.contentHashCode()
        return result
    }

    companion object {

        /**
         * Creates a PublicNodeIdentity object from the given advertisement data.
         *
         * The advertisement data must be 17 bytes long and the first byte must be 0x01.
         *
         * @param uuid UUID of the service.
         * @param advertisementData Advertisement data.
         * @return PublicNodeIdentity object or null if the advertisement data is invalid.
         */
        fun init(uuid: UUID, advertisementData: HashMap<UUID, ByteArray?>) =
            advertisementData[uuid]?.takeIf {
                it.size == 17 && it[0] == 0x01.toByte()
            }?.let {
                PublicNodeIdentity(
                    hash = it.sliceArray(1 until 9),
                    random = it.sliceArray(9 until 17)
                )
            }
    }
}

/**
 * Representation of Private Node Identity advertisement packet.
 *
 * @property hash    Function of the included random number and identity information.
 * @property random  64-bit random number.
 */
data class PrivateNodeIdentity internal constructor(
    val hash: ByteArray,
    val random: ByteArray
) : NodeIdentity {
    override fun matches(node: Node): Boolean {
        val data = ByteArray(5) { 0 } + random + node.primaryUnicastAddress.address.toByteArray()
        for (key in node.networkKeys) {
            if (isValidHash(hash = hash, data = data, identityKey = key.derivatives.identityKey))
                return true

            if (isValidHash(
                    hash = hash,
                    data = data,
                    identityKey = key.oldDerivatives?.identityKey
                )
            ) return true
        }
        return false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateNodeIdentity

        if (!hash.contentEquals(other.hash)) return false
        if (!random.contentEquals(other.random)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + random.contentHashCode()
        return result
    }

    companion object {

        /**
         * Creates a PrivateNodeIdentity object from the given advertisement data. The advertisement
         * data must be 17 bytes long and the first byte must be 0x03.
         *
         * @param uuid UUID of the service.
         * @param advertisementData Advertisement data.
         * @return PrivateNodeIdentity object or null if the advertisement data is invalid.
         */
        fun init(uuid: UUID, advertisementData: HashMap<UUID, ByteArray?>) =
            advertisementData[uuid]?.takeIf {
                it.size == 17 && it[0] == 0x03.toByte()
            }?.let {
                PrivateNodeIdentity(
                    hash = it.sliceArray(1 until 9),
                    random = it.sliceArray(9 until 17)
                )
            }
    }

}