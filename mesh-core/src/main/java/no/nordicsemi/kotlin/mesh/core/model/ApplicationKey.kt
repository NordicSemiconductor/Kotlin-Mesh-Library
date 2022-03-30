@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import kotlin.properties.Delegates

/**
 * Application Keys are used to secure communications at the upper transport layer.
 * The application key (AppKey) shall be generated using a random number generator
 * compatible with the requirements in Volume 2, Part H, Section 2 of the Core Specification [1].
 *
 * @property index         The index property contains an integer from 0 to 4095 that represents the NetKey index for this network key.
 * @property name          Human-readable name for the application functionality associated with this application key.
 * @property boundNetKeyIndex   The boundNetKey property contains a corresponding NetKey index from the netKeys property of the Mesh Object.
 * @property key           128-bit application key.
 * @property oldKey        OldKey property contains the previous application key.
 * @param    _key          128-bit application key.
 */
@Serializable
data class ApplicationKey internal constructor(
    val index: Int,
    @Serializable(with = KeySerializer::class)
    @SerialName("key")
    private var _key: ByteArray = Crypto.generateRandomKey()
) {
    var name: String by Delegates.observable(initialValue = "Application Key $index") { _, oldValue, newValue ->
        require(newValue.isNotBlank()) { "Application key cannot be empty!" }
        MeshNetwork.onChange(
            oldValue = oldValue,
            newValue = newValue,
            action = { network?.updateTimestamp() }
        )
    }

    @SerialName("boundNetKey")
    var boundNetKeyIndex: Int = 0
        internal set
    var key: ByteArray
        get() = _key
        internal set(value) {
            require(value = value.size == 16) { "Key must be 16-bytes long!" }
            _key = value
        }

    @Serializable(with = KeySerializer::class)
    var oldKey: ByteArray? = null
        internal set

    @Transient
    internal var network: MeshNetwork? = null

    @Transient
    var netKey: NetworkKey? = network?.networkKeys?.find {
        it.index == boundNetKeyIndex
    }
        private set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicationKey

        if (index != other.index) return false
        if (!_key.contentEquals(other._key)) return false
        if (name != other.name) return false
        if (boundNetKeyIndex != other.boundNetKeyIndex) return false
        if (oldKey != null) {
            if (other.oldKey == null) return false
            if (!oldKey.contentEquals(other.oldKey)) return false
        } else if (other.oldKey != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + _key.contentHashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + boundNetKeyIndex
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        return result
    }
}