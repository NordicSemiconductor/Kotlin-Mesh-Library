@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeyRefreshPhaseSerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.SecuritySerializer
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
 * @param security      Security property contains a string with a value of either “insecure” or “secure”, which describes a
 *                      minimum security level for a subnet associated with this network key. If all the nodes on the subnet
 *                      associated with this network key have been provisioned using the Secure Provisioning procedure,
 *                      then the value of minSecurity property for the subnet is set to “secure”; otherwise, the value of the
 *                      minSecurity is set to “insecure”.
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
    @SerialName(value = "minSecurity")
    @Serializable(with = SecuritySerializer::class)
    val security: Security,
    @SerialName(value = "oldKey")
    @Serializable(with = KeySerializer::class)
    var oldKey: ByteArray? = null,
    @Serializable(with = TimestampSerializer::class)
    val timestamp: Instant
) {

    internal constructor(
        name: String,
        index: Int,
        phase: KeyRefreshPhase,
        key: ByteArray,
        security: Security,
        timestamp: Instant
    ) : this(
        name = name,
        index = index,
        phase = phase,
        key = key,
        security = security,
        oldKey = null,
        timestamp = timestamp
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkKey

        if (name != other.name) return false
        if (index != other.index) return false
        if (phase != other.phase) return false
        if (!key.contentEquals(other.key)) return false
        if (security != other.security) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + index
        result = 31 * result + phase.hashCode()
        result = 31 * result + key.contentHashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}