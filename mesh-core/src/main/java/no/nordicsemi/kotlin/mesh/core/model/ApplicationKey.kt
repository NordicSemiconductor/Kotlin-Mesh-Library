package no.nordicsemi.kotlin.mesh.core.model

/**
 * Application Keys are used to secure communications at the upper transport layer.
 * The application key (AppKey) shall be generated using a random number generator
 * compatible with the requirements in Volume 2, Part H, Section 2 of the Core Specification [1].
 *
 * @param name			Human-readable name for the application functionality associated with this application key.
 * @param index			The index property contains an integer from 0 to 4095 that represents the NetKey index for this network key.
 * @param boundNetKey	The boundNetKey property contains a corresponding NetKey index from the netKeys property of the Mesh Object.
 * @param key			128-bit application key.
 * @param oldKey		OldKey property contains the previous application key.
 */
@Suppress("unused")
data class ApplicationKey internal constructor(
    val name: String,
    val index: Int,
    val boundNetKey: Int,
    val key: ByteArray,
    val oldKey: ByteArray
) {
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