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
 * @property deviceKey                  128-bit device key. When importing a partially exported
 *                                      network configuration, the device key might not be present
 *                                      in the Mesh Network Configuration Database.
 * @property security                   Represents the level of [Security] for the subnet on which
 *                                      the node has been originally provisioned.
 * @property netKeys                    Array of [NodeKey] that includes information about the
 *                                      network keys known to this node.
 * @property configComplete             True if the Mesh Manager determines that this node’s
 *                                      configuration process is completed; otherwise,
 *                                      the property’s value is set to false.
 * @property name                       Human-readable name that can identify this node within the
 *                                      mesh network.
 * @property companyIdentifier          16-bit Company Identifier (CID) assigned by the Bluetooth
 *                                      SIG. The value of this property is obtained from node
 *                                      composition data.
 * @property productIdentifier          16-bit, vendor-assigned Product Identifier (PID). The value
 *                                      of this property is obtained from node composition data.
 * @property versionIdentifier          16-bit, vendor-assigned product Version Identifier (VID).
 *                                      The value of this property is obtained from node composition
 *                                      data
 * @property replayProtectionCount      16-bit value indicating the minimum number of Replay
 *                                      Protection List (RPL) entries for this node. The value of
 *                                      this property is obtained from node composition data. RPL
 *                                      implementation handles a multi-segment message transaction
 *                                      which is under a replay attack. The sequence number of the
 *                                      last segment that has been received for this message is
 *                                      stored for that peer node in the replay protection list.
 * @property features                   [Features] supported by the node.
 * @property secureNetworkBeacon        Represents whether the node is configured to send Secure
 *                                      Network beacons.
 * @property defaultTTL                 0 to 127 that represents the default Time to Live (TTL)
 *                                      value used when sending messages.
 * @property networkTransmit            [NetworkTransmit] represents the parameters of the
 *                                      transmissions of network layer messages originating from a
 *                                      mesh node.
 * @property relayRetransmit            [RelayRetransmit] represents the parameters of the
 *                                      retransmissions of network layer messages relayed by a mesh
 *                                      node.
 * @property appKeys                    Array of [NodeKey] that includes information about the
 *                                      [ApplicationKey]s known to this node.
 * @property elements                   Array of elements contained in the Node.
 * @property excluded                   True if the node is in the process of being deleted and is
 *                                      excluded from the new network key distribution during the
 *                                      Key Refresh procedure; otherwise, it is set to “false”.
 * @property elementsCount              Number of elements belonging to this node.
 * @property addresses                  List of addresses used by this node.
 * @property unicastRange               Address range used by this node.
 * @property lastUnicastAddress         Address of the last element in the node.
 * @constructor                         Creates a mesh node.
 */
@Serializable
data class Node internal constructor(
    @SerialName(value = "UUID")
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    @Serializable(with = KeySerializer::class)
    val deviceKey: ByteArray?,
    @SerialName(value = "unicastAddress")
    internal var _primaryUnicastAddress: UnicastAddress,
    @SerialName(value = "elements")
    internal var _elements: MutableList<Element>,
    @SerialName(value = "netKeys")
    private var _netKeys: MutableList<NodeKey>,
    @SerialName(value = "appKeys")
    private var _appKeys: MutableList<NodeKey>,
) {

    internal constructor(
        provisioner: Provisioner,
        deviceKey: ByteArray,
        unicastAddress: UnicastAddress,
        elements: List<Element> = listOf(
            Element(
                Location.UNKNOWN,
                listOf(
                    Model(SigModelId(Model.CONFIGURATION_SERVER_MODEL_ID)),
                    Model(SigModelId(Model.CONFIGURATION_CLIENT_MODEL_ID))
                )
            )
        ),
        netKeys: List<NetworkKey>,
        appKeys: List<ApplicationKey>
    ) : this(
        uuid = provisioner.uuid,
        deviceKey = deviceKey,
        _primaryUnicastAddress = unicastAddress,
        _elements = elements.toMutableList(),
        _netKeys = MutableList(size = netKeys.size) { index -> NodeKey(netKeys[index]) },
        _appKeys = MutableList(size = appKeys.size) { index -> NodeKey(appKeys[index]) },
    )

    val primaryUnicastAddress: UnicastAddress
        get() = _primaryUnicastAddress

    var name: String = "nRF Mesh Node"
        set(value) {
            require(value = value.isNotBlank()) { "Name cannot be empty!" }
            network?.updateTimestamp()
            field = value
        }
    var netKeys: List<NodeKey>
        get() = _netKeys
        internal set(value) {
            _netKeys = value.toMutableList()
            network?.updateTimestamp()
        }
    var appKeys: List<NodeKey>
        get() = _appKeys
        internal set(value) {
            _appKeys = value.toMutableList()
            network?.updateTimestamp()
        }
    var elements: List<Element>
        get() = _elements
        internal set(value) {
            _elements = value.toMutableList()
            network?.updateTimestamp()
        }
    var security: Security = Insecure
        internal set
    var configComplete: Boolean = false
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }

    @Serializable(UShortAsStringSerializer::class)
    @SerialName(value = "cid")
    var companyIdentifier: UShort? = null
        internal set

    @Serializable(UShortAsStringSerializer::class)
    @SerialName(value = "pid")
    var productIdentifier: UShort? = null
        internal set

    @Serializable(UShortAsStringSerializer::class)
    @SerialName(value = "vid")
    var versionIdentifier: UShort? = null
        internal set

    @Serializable(UShortAsStringSerializer::class)
    @SerialName(value = "crpl")
    var replayProtectionCount: UShort? = null
        internal set
    var features: Features = Features(relay = null, proxy = null, friend = null, lowPower = null)
        internal set
    var secureNetworkBeacon: Boolean? = null
        internal set
    var networkTransmit: NetworkTransmit? = null
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }
    var relayRetransmit: RelayRetransmit? = null
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }
    var defaultTTL: Int = 127
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }
    var excluded: Boolean = false
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }

    @SerialName(value = "heartbeatPub")
    var heartbeatPublication: HeartbeatPublication? = null
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }

    @SerialName(value = "heartbeatSub")
    var heartbeatSubscription: HeartbeatSubscription? = null
        internal set(value) {
            field = value
            network?.updateTimestamp()
        }

    val elementsCount: Int
        get() = elements.size

    val addresses: List<UnicastAddress>
        get() = List(elementsCount) { index -> _primaryUnicastAddress + index }

    val unicastRange: UnicastRange
        get() = UnicastRange(_primaryUnicastAddress, elementsCount)

    val lastUnicastAddress: UnicastAddress
        get() = _primaryUnicastAddress + when (elementsCount > 0) {
            true -> elementsCount
            false -> 1 // TODO should we throw here?
        } - 1

    init {
        require(elements.isNotEmpty()) {
            throw IllegalArgumentException("At least one element is mandatory!")
        }
        elements.let {
            it.forEachIndexed { index, element ->
                // Assigns the index based on position in the list of elements.
                element.index = index
                // Assigns the current node as the parent node of the element.
                element.parentNode = this
            }
        }
    }

    @Transient
    internal var network: MeshNetwork? = null

    /**
     * Adds a network key to a node.
     *
     * @param key     Network key to be added.
     * @return        True if success or false if the key already exists.
     */
    internal fun add(key: NetworkKey) = NodeKey(key = key).run {
        when {
            _netKeys.contains(this) -> false
            else -> {
                _netKeys.add(this)
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
    internal fun add(key: ApplicationKey) = NodeKey(key = key).run {
        when {
            _appKeys.contains(this) -> false
            else -> {
                _appKeys.add(this)
                network?.updateTimestamp()
                true
            }
        }
    }

    /**
     * Adds an Element to a node.
     *
     * @param element     Element to be added.
     * @return            True if success or false if the element already exists.
     */
    internal fun add(element: Element): Boolean = when {
        _elements.contains(element) -> false
        else -> {
            _elements.add(element)
            true
        }
    }

    /**
     * Checks if the given addresses used by the specified number of elements overlaps with the
     * address range used by the node.
     *
     * @param address       Desired unicast address.
     * @param count         Number of elements.
     * @return true if the address range is in use.
     */
    fun overlaps(address: UnicastAddress, count: Int) = try {
        !(_primaryUnicastAddress + (elementsCount - 1) < address ||
                _primaryUnicastAddress > address + (count - 1))
    } catch (e: IllegalArgumentException) {
        true
    }

    /**
     * Checks if an element in the node uses this address.
     *
     * @param address Unicast address.
     * @return true if the given address is in use by any of the elements
     */
    fun containsElementWithAddress(address: UnicastAddress) = elements.any {
        it.unicastAddress == address
    }

    /**
     * Checks if an element in the node has a Unicast Address from the given range.
     *
     * @param range Unicast Range.
     * @return true if given range overlaps with the node's address range.
     */
    fun containsElementsWithAddress(range: UnicastRange) = unicastRange.overlaps(range)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (uuid != other.uuid) return false
        if (!deviceKey.contentEquals(other.deviceKey)) return false
        if (_primaryUnicastAddress != other._primaryUnicastAddress) return false
        if (_elements != other._elements) return false
        if (_netKeys != other._netKeys) return false
        if (_appKeys != other._appKeys) return false
        if (name != other.name) return false
        if (security != other.security) return false
        if (configComplete != other.configComplete) return false
        if (companyIdentifier != other.companyIdentifier) return false
        if (productIdentifier != other.productIdentifier) return false
        if (versionIdentifier != other.versionIdentifier) return false
        if (replayProtectionCount != other.replayProtectionCount) return false
        if (features != other.features) return false
        if (secureNetworkBeacon != other.secureNetworkBeacon) return false
        if (networkTransmit != other.networkTransmit) return false
        if (relayRetransmit != other.relayRetransmit) return false
        if (defaultTTL != other.defaultTTL) return false
        if (excluded != other.excluded) return false
        if (heartbeatPublication != other.heartbeatPublication) return false
        if (heartbeatSubscription != other.heartbeatSubscription) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + deviceKey.contentHashCode()
        result = 31 * result + _primaryUnicastAddress.hashCode()
        result = 31 * result + _elements.hashCode()
        result = 31 * result + _netKeys.hashCode()
        result = 31 * result + _appKeys.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + security.hashCode()
        result = 31 * result + configComplete.hashCode()
        result = 31 * result + (companyIdentifier?.hashCode() ?: 0)
        result = 31 * result + (productIdentifier?.hashCode() ?: 0)
        result = 31 * result + (versionIdentifier?.hashCode() ?: 0)
        result = 31 * result + (replayProtectionCount?.hashCode() ?: 0)
        result = 31 * result + features.hashCode()
        result = 31 * result + (secureNetworkBeacon?.hashCode() ?: 0)
        result = 31 * result + (networkTransmit?.hashCode() ?: 0)
        result = 31 * result + (relayRetransmit?.hashCode() ?: 0)
        result = 31 * result + defaultTTL
        result = 31 * result + excluded.hashCode()
        result = 31 * result + (heartbeatPublication?.hashCode() ?: 0)
        result = 31 * result + (heartbeatSubscription?.hashCode() ?: 0)
        return result
    }
}