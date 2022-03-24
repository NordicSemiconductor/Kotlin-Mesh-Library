@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto

/**
 * Application Keys are used to secure communications at the upper transport layer.
 * The application key (AppKey) shall be generated using a random number generator
 * compatible with the requirements in Volume 2, Part H, Section 2 of the Core Specification [1].
 *
 * @property index         The index property contains an integer from 0 to 4095 that represents the NetKey index for this network key.
 * @property name          Human-readable name for the application functionality associated with this application key.
 * @property boundNetKey   The boundNetKey property contains a corresponding NetKey index from the netKeys property of the Mesh Object.
 * @property key           128-bit application key.
 * @property oldKey        OldKey property contains the previous application key.
 */
@Serializable
data class ApplicationKey internal constructor(val index: Int) {
    var name: String = "Application Key $index"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty" }
            field = value
        }
    var boundNetKey: Int = 0
        internal set

    @Serializable(with = KeySerializer::class)
    lateinit var key: ByteArray
        internal set

    @Serializable(with = KeySerializer::class)
    var oldKey: ByteArray? = null
        internal set

    /**
     * Secondary constructor for creating an application key object.
     * @param index   The index property contains an integer from 0 to 4095 that represents the NetKey index for this network key.
     * @param key     128-bit application key.
     */
    internal constructor(index: Int, key: ByteArray) : this(index = index) {
        this.key = key
    }

    init {
        // Let's check to ensure if the key is already initialized using the secondary constructor.
        if (!::key.isInitialized) key = Crypto.generateRandomKey()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApplicationKey

        if (name != other.name) return false
        if (index != other.index) return false
        if (boundNetKey != other.boundNetKey) return false
        if (!key.contentEquals(other.key)) return false
        if (!oldKey.contentEquals(other.oldKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + index
        result = 31 * result + boundNetKey
        result = 31 * result + key.contentHashCode()
        result = 31 * result + oldKey.contentHashCode()
        return result
    }
}





































