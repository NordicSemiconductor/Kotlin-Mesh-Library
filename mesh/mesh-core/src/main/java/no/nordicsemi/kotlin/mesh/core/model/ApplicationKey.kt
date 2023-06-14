@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.core.exception.KeyInUse
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork.Companion.onChange
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto

/**
 * Application Keys are used to secure communications at the upper transport layer.
 * The application key (AppKey) shall be generated using a random number generator
 * compatible with the requirements in Volume 2, Part H, Section 2 of the Core Specification [1].
 *
 * @property index              The index property contains an integer from 0 to 4095 that
 *                              represents the NetKey index for this network key.
 * @property name               Human-readable name for the application functionality associated
 *                              with this application key.
 * @property boundNetKeyIndex   The boundNetKey property contains a corresponding Network Key index
 *                              of the network key in the mesh network.
 * @property key                128-bit application key.
 * @property oldKey             OldKey property contains the previous application key.
 * @property netKey             Network key to which this application key is bound to.
 * @param    _key               128-bit application key.
 */
@Serializable
data class ApplicationKey internal constructor(
    @SerialName(value = "name")
    private var _name: String,
    val index: KeyIndex,
    @Serializable(with = KeySerializer::class)
    @SerialName("key")
    private var _key: ByteArray = Crypto.generateRandomKey()
) {
    var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank()) { "Name cannot be empty!" }
            onChange(oldValue = _name, newValue = value) { network?.updateTimestamp() }
            _name = value
        }

    @SerialName("boundNetKey")
    var boundNetKeyIndex: KeyIndex = 0u

    var key: ByteArray
        get() = _key
        internal set(value) {
            require(value.size == 16) { "Key must be 16-bytes long!" }
            _key = value
        }

    @Serializable(with = KeySerializer::class)
    var oldKey: ByteArray? = null
        internal set

    @Transient
    internal var network: MeshNetwork? = null

    val netKey: NetworkKey?
        get() = network?._networkKeys?.find { networkKey ->
            networkKey.index == boundNetKeyIndex
        }

    init {
        require(index.isValidKeyIndex()) { "Key index must be in range from 0 to 4095." }
    }

    /**
     * Returns whether the application key is added to any nodes in the network.
     * A key that is in use cannot be removed until it has been removed from all the nodes.
     */
    fun isInUse(): Boolean = network?.run {
        // The application key in used when it is known by any of the nodes in the network.
        _nodes.any { node ->
            node.appKeys.any { nodeKey ->
                nodeKey.index == index
            }
        }
    } ?: false

    /**
     * Updates the existing key with the given key, if it is not in use.
     *
     * @param key New key.
     * @throws KeyInUse If the key is already in use.
     */
    fun setKey(key: ByteArray) {
        require(!isInUse()) { throw KeyInUse }
        require(key.size == 16) { throw InvalidKeyLength }
        _key = key
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicationKey

        if (index != other.index) return false
        if (!_key.contentEquals(other._key)) return false
        if (boundNetKeyIndex != other.boundNetKeyIndex) return false
        if (oldKey != null) {
            if (other.oldKey == null) return false
            if (!oldKey.contentEquals(other.oldKey)) return false
        } else if (other.oldKey != null) return false
        if (netKey != other.netKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + _key.contentHashCode()
        result = 31 * result + boundNetKeyIndex.hashCode()
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        result = 31 * result + (netKey?.hashCode() ?: 0)
        return result
    }

}