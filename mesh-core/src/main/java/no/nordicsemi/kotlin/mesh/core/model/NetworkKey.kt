@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto

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
    val index: KeyIndex,
    @SerialName(value = "name")
    private var _name: String,
    @Serializable(with = KeySerializer::class)
    @SerialName("key")
    private var _key: ByteArray = Crypto.generateRandomKey(),
    @SerialName(value = "minSecurity")
    private var _security: Security = Secure,
    @SerialName(value = "phase")
    private var _phase: KeyRefreshPhase = NormalOperation,
) {
    var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank()) { "Name cannot be empty!" }
            MeshNetwork.onChange(oldValue = _name, newValue = value) { network?.updateTimestamp() }
            _name = value
        }
    var security: Security
        get() = _security
        internal set(value) {
            // Security can only be downgraded.
            if (_security is Secure)
                _security = value
        }
    var phase: KeyRefreshPhase
        get() = _phase
        set(value) {
            MeshNetwork.onChange(oldValue = _phase, newValue = value) { updateTimeStamp() }
            _phase = value
        }
    var key: ByteArray
        get() = _key
        internal set(value) {
            require(value = value.size == 16) { "Key must be 16-bytes!" }
            _key = value
        }

    @Serializable(with = KeySerializer::class)
    @SerialName(value = "oldKey")
    var oldKey: ByteArray? = null
        internal set

    var timestamp: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        internal set

    @Transient
    internal var network: MeshNetwork? = null

    init {
        require(index.isValidKeyIndex()) { "Key index must be in range from 0 to 4095." }
    }

    private fun updateTimeStamp() {
        timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    }

    /**
     * Returns whether the network key is added to any nodes in the network.
     * A key that is in use cannot be removed until it has been removed from all the nodes and is no longer
     * bound to any application keys.
     */
    fun isInUse(): Boolean = network?.run {
        // A network key is in use if at least one application key is bound to it.
        // OR
        // The network key is known by any of the nodes in the network.
        _applicationKeys.none { applicationKey ->
            applicationKey.boundNetKeyIndex == index
        } || _nodes.none { node ->
            node.netKeys.any { nodeKey ->
                nodeKey.index == index
            }
        }
    } ?: false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkKey

        if (index != other.index) return false
        if (!_key.contentEquals(other._key)) return false
        if (_security != other._security) return false
        if (oldKey != null) {
            if (other.oldKey == null) return false
            if (!oldKey.contentEquals(other.oldKey)) return false
        } else if (other.oldKey != null) return false
        if (timestamp != other.timestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = index.hashCode()
        result = 31 * result + _key.contentHashCode()
        result = 31 * result + _security.hashCode()
        result = 31 * result + (oldKey?.contentHashCode() ?: 0)
        result = 31 * result + timestamp.hashCode()
        return result
    }

    companion object {
        private const val MIN_KEY_INDEX = 0
        private const val MAX_KEY_INDEX = 4095
    }
}
