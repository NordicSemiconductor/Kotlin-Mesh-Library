@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.model.serialization.KeySerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.UShortAsStringSerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import java.util.*

/**
 * The node represents a configured state of a mesh node.
 *
 * @property uuid                       Unique 128-bit UUID of the node.
 * @property deviceKey                  128-bit device key.
 * @property security                   Represents the level of [Security] for the subnet on which the node has been originally provisioned.
 * @property netKeys                    Array of [NodeKey] that includes information about the network keys known to this node.
 * @property configComplete             True if the Mesh Manager determines that this node’s configuration process is completed; otherwise,
 *                                      the property’s value is set to false.
 * @property name                       Human-readable name that can identify this node within the mesh network.
 * @property cid                        16-bit Company Identifier (CID) assigned by the Bluetooth SIG. The value of this property is obtained from node composition data.
 * @property pid                        16-bit, vendor-assigned Product Identifier (PID). The value of this property is obtained from node composition data.
 * @property vid                        16-bit, vendor-assigned product Version Identifier (VID). The value of this property is obtained from node composition data
 * @property crpl                       16-bit value indicating the minimum number of Replay Protection List (RPL) entries for this node.
 *                                      The value of this property is obtained from node composition data. RPL implementation handles a multi-segment message
 *                                      transaction which is under a replay attack. The sequence number of the last segment that has been received for this message
 *                                      is stored for that peer node in the replay protection list.
 * @property features                   [Features] supported by the node.
 * @property secureNetworkBeacon        Represents whether the node is configured to send Secure Network beacons.
 * @property defaultTTL                 0 to 127 that represents the default Time to Live (TTL) value used when sending messages.
 * @property networkTransmit            [NetworkTransmit] represents the parameters of the transmissions of network layer messages originating from a mesh node.
 * @property relayRetransmit            [RelayRetransmit] represents the parameters of the retransmissions of network layer messages relayed by a mesh node.
 * @property appKeys                    Array of [NodeKey] that includes information about the [ApplicationKey]s known to this node.
 * @property elements                   Array of elements contained in the Node.
 * @property excluded                   True if the node is in the process of being deleted and is excluded from the new network key distribution during the
 *                                      Key Refresh procedure; otherwise, it is set to “false”.
 *
 */
@Serializable
data class Node internal constructor(
    @SerialName(value = "UUID")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    @Serializable(with = KeySerializer::class)
    val deviceKey: ByteArray,
    val unicastAddress: UnicastAddress,
    val elements: List<Element>,
    private var _netKeys: List<NodeKey>,
    private var _appKeys: List<NodeKey>,
) {
    var name: String = "Mesh Network"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty!" }
            network?.updateTimestamp()
            field = value
        }
    var netKeys: List<NodeKey>
        get() = _netKeys
        internal set(value) {
            _netKeys = value
            network?.updateTimestamp()
        }

    var appKeys: List<NodeKey>
        get() = _appKeys
        internal set(value) {
            _appKeys = value
            network?.updateTimestamp()
        }

    var security: Security = Insecure
        internal set
    var configComplete: Boolean = false
        internal set

    @Serializable(UShortAsStringSerializer::class)
    var cid: UShort? = null
        internal set

    @Serializable(UShortAsStringSerializer::class)
    var pid: UShort? = null
        internal set

    @Serializable(UShortAsStringSerializer::class)
    var vid: UShort? = null
        internal set

    @Serializable(UShortAsStringSerializer::class)
    var crpl: UShort? = null
        internal set
    var features: Features = Features(relay = null, proxy = null, friend = null, lowPower = null)
        internal set
    var secureNetworkBeacon: Boolean? = null
        internal set
    var networkTransmit: NetworkTransmit? = null
        internal set
    var relayRetransmit: RelayRetransmit? = null
        internal set
    var defaultTTL: Int = 127
        internal set
    var excluded: Boolean = false
        internal set
    var heartbeatPublication: HeartbeatPublication? = null
        internal set
    var heartbeatSubscription: HeartbeatSubscription? = null
        internal set

    @Transient
    internal var network: MeshNetwork? = null

    /**
     * Adds a network key to a node.
     *
     * @param key     Network key to be added.
     * @return        True if success or false if the key already exists.
     */
    internal fun addKey(key: NetworkKey) = NodeKey(key = key).run {
        when {
            _netKeys.contains(this) -> false
            else -> {
                _netKeys = _netKeys + this
                network?.updateTimestamp()
                true
            }
        }
    }

    /**
     * Adds an application key to a node.
     *
     * @param key     Application key to be added.
     * @return        True if success or false if the key already exists.
     */
    internal fun addKey(key: ApplicationKey) = NodeKey(key = key).run {
        when {
            _appKeys.contains(this) -> false
            else -> {
                _appKeys = _appKeys + this
                network?.updateTimestamp()
                true
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (uuid != other.uuid) return false
        if (!deviceKey.contentEquals(other.deviceKey)) return false
        if (unicastAddress != other.unicastAddress) return false
        if (elements != other.elements) return false
        if (_netKeys != other._netKeys) return false
        if (_appKeys != other._appKeys) return false
        if (name != other.name) return false
        if (security != other.security) return false
        if (configComplete != other.configComplete) return false
        if (cid != other.cid) return false
        if (pid != other.pid) return false
        if (vid != other.vid) return false
        if (crpl != other.crpl) return false
        if (features != other.features) return false
        if (secureNetworkBeacon != other.secureNetworkBeacon) return false
        if (networkTransmit != other.networkTransmit) return false
        if (relayRetransmit != other.relayRetransmit) return false
        if (defaultTTL != other.defaultTTL) return false
        if (excluded != other.excluded) return false
        if (heartbeatPublication != other.heartbeatPublication) return false
        if (heartbeatSubscription != other.heartbeatSubscription) return false
        if (network != other.network) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + deviceKey.contentHashCode()
        result = 31 * result + unicastAddress.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + _netKeys.hashCode()
        result = 31 * result + _appKeys.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + configComplete.hashCode()
        result = 31 * result + (cid?.hashCode() ?: 0)
        result = 31 * result + (pid?.hashCode() ?: 0)
        result = 31 * result + (vid?.hashCode() ?: 0)
        result = 31 * result + (crpl?.hashCode() ?: 0)
        result = 31 * result + features.hashCode()
        result = 31 * result + (secureNetworkBeacon?.hashCode() ?: 0)
        result = 31 * result + (networkTransmit?.hashCode() ?: 0)
        result = 31 * result + (relayRetransmit?.hashCode() ?: 0)
        result = 31 * result + defaultTTL
        result = 31 * result + excluded.hashCode()
        result = 31 * result + (heartbeatPublication?.hashCode() ?: 0)
        result = 31 * result + (heartbeatSubscription?.hashCode() ?: 0)
        result = 31 * result + (network?.hashCode() ?: 0)
        return result
    }
}