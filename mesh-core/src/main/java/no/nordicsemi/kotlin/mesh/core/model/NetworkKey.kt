package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeyRefreshPhaseSerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.TimestampSerializer

/**
 * Application Keys are used to secure communications at the upper transport layer.
 * The application key (AppKey) shall be generated using a random number generator
 * compatible with the requirements in Volume 2, Part H, Section 2 of the Core Specification [1].
 *
 * @param name          Human-readable name for the application functionality associated with this application key.
 * @param index         The index property contains an integer from 0 to 4095 that represents the NetKey index for this network key.
 * @param phase         The phase property represents the [KeyRefreshPhase] for the subnet associated with this network key.
 * @param key           128-bit application key.
 * @param minSecurity   A
 * @param oldKey        OldKey property contains the previous application key.
 */
@Serializable
data class NetworkKey internal constructor(
    val name: String,
    val index: Int,
    @Serializable(with = KeyRefreshPhaseSerializer::class)
    val phase: KeyRefreshPhase,
    @Serializable(with = KeySerializer::class)
    val key: ByteArray,
    val minSecurity: Security,
    @Serializable(with = KeySerializer::class)
    var oldKey: ByteArray?,
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkKey

        if (name != other.name) return false
        if (index != other.index) return false
        if (phase != other.phase) return false
        if (!key.contentEquals(other.key)) return false
        if (minSecurity != other.minSecurity) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + index
        result = 31 * result + phase.hashCode()
        result = 31 * result + key.contentHashCode()
        result = 31 * result + minSecurity.hashCode()
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}