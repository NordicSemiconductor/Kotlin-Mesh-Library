@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.util

import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.util.Utils.toByteArray
import no.nordicsemi.kotlin.mesh.crypto.Crypto

/**
 * The Node Identity contains information from Node Identity or Private Node Identity beacon.
 *
 * This can be used to identify a node in the network.
 */
interface NodeIdentity {

    /**
     * Returns whether the identity matches given node.
     *
     * @param node Node to be matched.
     * @return true if the identity matches, false otherwise.
     */
    fun matches(node: Node): Boolean
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
            val calculatedHash =
                Crypto.calculateHash(data = data, identityKey = key.derivatives.identityKey)
            if (hash.contentEquals(calculatedHash))
                return true

            key.oldDerivatives?.identityKey?.let {
                if (hash.contentEquals(Crypto.calculateHash(data = data, identityKey = it)))
                    return true
            }
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
            val calculatedHash =
                Crypto.calculateHash(data = data, identityKey = key.derivatives.identityKey)
            if (hash.contentEquals(calculatedHash))
                return true

            key.oldDerivatives?.identityKey?.let {
                if (hash.contentEquals(Crypto.calculateHash(data = data, identityKey = it)))
                    return true
            }
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
}

/**
 * Returns the Node Identity for a given Node Identity beacon.
 *
 * @receiver ByteArray Node Identity beacon.
 * @return NodeIdentity or null if the beacon is invalid.
 */
fun ByteArray.nodeIdentity() = when {
    size == 17 && get(0) == 0x01.toByte() -> PublicNodeIdentity(
        hash = sliceArray(1 until 9),
        random = sliceArray(9 until 17)
    )

    size == 17 && get(0) == 0x03.toByte() -> PrivateNodeIdentity(
        hash = sliceArray(1 until 9),
        random = sliceArray(9 until 17)
    )

    else -> null
}