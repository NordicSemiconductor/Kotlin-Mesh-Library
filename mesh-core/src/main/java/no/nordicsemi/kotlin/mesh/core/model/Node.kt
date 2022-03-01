package no.nordicsemi.kotlin.mesh.core.model

import java.util.*

/**
 * The node represents a configured state of a mesh node.
 *
 * @property uuid                       128-bit device uuid.
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
 *
 */
data class Node internal constructor(
    val uuid: UUID,
    val deviceKey: ByteArray,
    val security: Security,
    val netKeys: Array<NodeKey>,
    val configComplete: Boolean,
    val name: String,
    val cid: Int,
    val pid: Int,
    val vid: Int,
    val crpl: Int,
    val features: Features,
    val secureNetworkBeacon: Boolean,
    val unicastAddress: UnicastAddress,
    val elements: Array<Element>,
    val appKeys: Array<NodeKey>,
    val networkTransmit: NetworkTransmit,
    val relayRetransmit: RelayRetransmit,
    val defaultTTL: Int,
    val excluded: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (uuid != other.uuid) return false
        if (name != other.name) return false
        if (!deviceKey.contentEquals(other.deviceKey)) return false
        if (unicastAddress != other.unicastAddress) return false
        if (security != other.security) return false
        if (cid != other.cid) return false
        if (pid != other.pid) return false
        if (vid != other.vid) return false
        if (crpl != other.crpl) return false
        if (features != other.features) return false
        if (!elements.contentEquals(other.elements)) return false
        if (configComplete != other.configComplete) return false
        if (!netKeys.contentEquals(other.netKeys)) return false
        if (!appKeys.contentEquals(other.appKeys)) return false
        if (networkTransmit != other.networkTransmit) return false
        if (defaultTTL != other.defaultTTL) return false
        if (excluded != other.excluded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + deviceKey.contentHashCode()
        result = 31 * result + unicastAddress.hashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + cid
        result = 31 * result + pid
        result = 31 * result + vid
        result = 31 * result + crpl
        result = 31 * result + features.hashCode()
        result = 31 * result + elements.contentHashCode()
        result = 31 * result + configComplete.hashCode()
        result = 31 * result + netKeys.contentHashCode()
        result = 31 * result + appKeys.contentHashCode()
        result = 31 * result + networkTransmit.hashCode()
        result = 31 * result + defaultTTL
        result = 31 * result + excluded.hashCode()
        return result
    }
}