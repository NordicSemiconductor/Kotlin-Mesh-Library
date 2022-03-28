@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.TimestampSerializer

/**
 * AThe network key object represents the state of the mesh network key that is used for securing
 * communication at the network layer.
 *
 * @property index         The index property contains an integer from 0 to 4095 that represents the NetKey index for this network key.
 * @property key           128-bit key.
 * @property security      Security property contains a string with a value of either “insecure” or “secure”, which describes a
 *                         minimum security level for a subnet associated with this network key. If all the nodes on the subnet
 *                         associated with this network key have been provisioned using the Secure Provisioning procedure,
 *                         then the value of minSecurity property for the subnet is set to “secure”; otherwise, the value of the
 *                         minSecurity is set to “insecure”.
 * @property name          Human-readable name for the the mesh subnet associated with this network key.
 * @property phase         The phase property represents the [KeyRefreshPhase] for the subnet associated with this network key.
 * @property oldKey        The oldKey property contains a 32-character hexadecimal string that represents the 128-bit network key,
 *                         and shall be present when the phase property has a non-zero value, such as when the Key Refresh
 *                         procedure is in progress. The value of the oldKey property contains the previous network key.
 * @property timestamp     The timestamp property contains a string that represents the last time the value of the phase property has
 *                         been updated.
 */
@Serializable
data class NetworkKey internal constructor(
    val index: Int,
    @Serializable(with = KeySerializer::class)
    @SerialName("key")
    private var _key: ByteArray,
    @SerialName(value = "minSecurity")
    val security: Security
) {
    var name: String = "Network Key"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty!" }
            if (field != value)
                network?.updateTimestamp()
            field = value
        }
    var phase: KeyRefreshPhase = NormalOperation
        internal set(value) {
            if (field != value)
                updateTimeStamp()
            field = value
        }

    var key: ByteArray
        get() = _key
        internal set(value) {
            require(value = value.isNotEmpty()) { "Key cannot be empty!" }
            this._key = value
        }

    @Serializable(with = KeySerializer::class)
    @SerialName(value = "oldKey")
    var oldKey: ByteArray? = null
        internal set

    @Serializable(with = TimestampSerializer::class)
    var timestamp: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        internal set

    @Transient
    internal var network: MeshNetwork? = null

    private fun updateTimeStamp() {
        timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkKey

        if (index != other.index) return false
        if (!_key.contentEquals(other._key)) return false
        if (security != other.security) return false
        if (name != other.name) return false
        if (phase != other.phase) return false
        if (oldKey != null) {
            if (other.oldKey == null) return false
            if (!oldKey.contentEquals(other.oldKey)) return false
        } else if (other.oldKey != null) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index
        result = 31 * result + _key.contentHashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + phase.hashCode()
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }
}