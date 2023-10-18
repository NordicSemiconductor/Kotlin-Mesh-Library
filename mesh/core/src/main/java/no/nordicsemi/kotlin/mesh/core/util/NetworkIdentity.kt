@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.util

import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.crypto.Crypto

/**
 * The Network Identity contains information from Network Identity or Private Network Identity
 * beacon.
 *
 */
sealed interface NetworkIdentity {

    /**
     * Returns whether the identity matches given network key.
     *
     * @param networkKey Network key to be matched.
     * @return true if the identity matches, false otherwise.
     */
    fun matches(networkKey: NetworkKey): Boolean

}

/**
 * Represents a Private Node Identity advertisement packet.
 *
 * @param networkId 64-bit network identifier derived from the network key.
 */
data class PublicNetworkIdentity(val networkId: ByteArray) : NetworkIdentity {

    override fun matches(networkKey: NetworkKey) = networkKey.networkId.contentEquals(networkId) ||
            networkKey.oldNetworkId.contentEquals(networkId)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicNetworkIdentity

        if (!networkId.contentEquals(other.networkId)) return false

        return true
    }

    override fun hashCode(): Int {
        return networkId.contentHashCode()
    }
}

/**
 * Represents a Private Node Identity advertisement packet.
 *
 * @param hash    Function of the included random number and identity information.
 * @param random  64-bit random number.
 */
data class PrivateNetworkIdentity(val hash: ByteArray, val random: ByteArray) : NetworkIdentity {

    override fun matches(networkKey: NetworkKey): Boolean {
        val data = networkKey.networkId + random
        val calculatedHash = Crypto.calculateHash(data, networkKey.derivatives.identityKey)
        if (calculatedHash.contentEquals(hash)) return true
        return networkKey.takeIf {
            it.oldDerivatives != null && it.oldNetworkId != null
        }?.let {
            val oldData = it.oldNetworkId!! + random
            hash.contentEquals(Crypto.calculateHash(oldData, it.oldDerivatives!!.identityKey))
        } ?: false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateNetworkIdentity

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