@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import java.util.*

/**
 * The node represents a configured state of a mesh node.
 *
 * @property uuid                       128-bit uuid.
 * @property deviceKey                  128-bit device key.
 * @property security                   Represents the level of [Security] for the subnet on which the node has been originally provisioned.
 * @property netKeys                    Array of [NodeKey] that includes information about the network keys known to this node.
 * @property configComplete             True of the Mesh Manager determines that this node’s configuration process is completed; otherwise,
 *                                      the property’s value is set to “false”.
 * @property name                       Human-readable name that can identify this node within the mesh network.
 * @property cid                        16-bit Company Identifier (CID) assigned by the Bluetooth SIG. The value of this property is obtained from node composition data.
 * @property pid                        16-bit, vendor-assigned Product Identifier (PID). The value of this property is obtained from node composition data.
 * @property vid                        16-bit, vendor-assigned product Version Identifier (VID). The value of this property is obtained from node composition data
 * @property crpl                       16-bit value indicating the minimum number of Replay Protection List (RPL) entries for this node.
 *                                      The value of this property is obtained from node composition data.
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
data class Node(
    val uuid: UUID,
    val deviceKey: ByteArray,
    val netKeys: List<NodeKey>,
    val name: String,
    val unicastAddress: UnicastAddress,
    val elements: List<Element>,
    val appKeys: List<NodeKey>,
) {

    var security: Security = Security.INSECURE
        internal set
    var configComplete: Boolean = false
        internal set
    var cid: Int? = null
        internal set
    var pid: Int? = null
        internal set
    var vid: Int? = null
        internal set
    var crpl: Int? = null
        internal set
    var features: Features = Features(relay = null, proxy = null, friend = null, lowPower = null)
        internal set
    var secureNetworkBeacon: Boolean = false
    var networkTransmit: NetworkTransmit? = null
        internal set
    var relayRetransmit: RelayRetransmit? = null
        internal set
    var defaultTTL: Int = 127
        internal set
    var excluded: Boolean = false
        internal set

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (uuid != other.uuid) return false
        if (!deviceKey.contentEquals(other.deviceKey)) return false
        if (netKeys != other.netKeys) return false
        if (name != other.name) return false
        if (unicastAddress != other.unicastAddress) return false
        if (elements != other.elements) return false
        if (appKeys != other.appKeys) return false
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

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + deviceKey.contentHashCode()
        result = 31 * result + netKeys.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + unicastAddress.hashCode()
        result = 31 * result + elements.hashCode()
        result = 31 * result + appKeys.hashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + configComplete.hashCode()
        result = 31 * result + (cid ?: 0)
        result = 31 * result + (pid ?: 0)
        result = 31 * result + (vid ?: 0)
        result = 31 * result + (crpl ?: 0)
        result = 31 * result + features.hashCode()
        result = 31 * result + secureNetworkBeacon.hashCode()
        result = 31 * result + (networkTransmit?.hashCode() ?: 0)
        result = 31 * result + (relayRetransmit?.hashCode() ?: 0)
        result = 31 * result + defaultTTL
        result = 31 * result + excluded.hashCode()
        return result
    }
}