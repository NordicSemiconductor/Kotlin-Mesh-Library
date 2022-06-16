@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.exception.*
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import java.util.*

/**
 * MeshNetwork representing a Bluetooth mesh network.
 *
 * @property uuid                   128-bit Universally Unique Identifier (UUID), which allows
 *                                  differentiation among multiple mesh networks.
 * @property name                   Human-readable name for the mesh network.
 * @property timestamp              Represents the last time the Mesh Object has been modified. The
 *                                  timestamp is based on Coordinated Universal Time.
 * @property partial                Indicates if this Mesh Configuration Database is part of a
 *                                  larger database.
 * @property networkKeys            List of network keys that includes information about network
 *                                  keys used in the mesh network.
 * @property applicationKeys        List of app keys that includes information about app keys used
 *                                  in the mesh network.
 * @property provisioners           List of known Provisioners and ranges of addresses that have
 *                                  been allocated to these Provisioners.
 * @property nodes                  List of nodes that includes information about mesh nodes in the
 *                                  mesh network.
 * @property groups                 List of groups that includes information about groups configured
 *                                  in the mesh network.
 * @property scenes                 List of scenes that includes information about scenes configured
 *                                  in the mesh network.
 * @property networkExclusions      List of excluded addresses per IvIndex.
 * @property ivIndex                IV Index of the network received via the last Secure Network
 *                                  Beacon and its current state.
 * @property localProvisioner       Main provisioner of the network which is the first provisioner
 *                                  in the list of provisioners.
 * @constructor                     Creates a mesh network.
 */
@Serializable
class MeshNetwork internal constructor(
    @Serializable(with = UUIDSerializer::class)
    @SerialName(value = "meshUUID")
    val uuid: UUID = UUID.randomUUID(),
    @SerialName(value = "meshName")
    private var _name: String
) {
    var name: String
        get() = _name
        set(value) {
            require(value.isNotBlank()) { "Name cannot be empty!" }
            onChange(oldValue = _name, newValue = value) { updateTimestamp() }
            _name = value
        }
    var timestamp: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        internal set

    @Suppress("RedundantSetter")
    var partial: Boolean = false
        internal set(value) {
            onChange(oldValue = field, newValue = value) { updateTimestamp() }
            field = value
        }

    @SerialName("provisioners")
    internal var _provisioners: MutableList<Provisioner> = mutableListOf()
    val provisioners: List<Provisioner>
        get() = _provisioners

    @SerialName("netKeys")
    internal var _networkKeys: MutableList<NetworkKey> = mutableListOf()
    val networkKeys: List<NetworkKey>
        get() = _networkKeys

    @SerialName("appKeys")
    internal var _applicationKeys: MutableList<ApplicationKey> = mutableListOf()
    val applicationKeys: List<ApplicationKey>
        get() = _applicationKeys

    @SerialName("nodes")
    internal var _nodes: MutableList<Node> = mutableListOf()
    val nodes: List<Node>
        get() = _nodes

    @SerialName("groups")
    internal var _groups: MutableList<Group> = mutableListOf()
    val groups: List<Group>
        get() = _groups

    @SerialName("scenes")
    internal var _scenes: MutableList<Scene> = mutableListOf()
    val scenes: List<Scene>
        get() = _scenes

    @SerialName("networkExclusions")
    internal var _networkExclusions: MutableList<ExclusionList> = mutableListOf()
    internal val networkExclusions: List<ExclusionList>
        get() = _networkExclusions

    @Transient
    internal var ivIndex = IvIndex()

    val localProvisioner: Provisioner
        get() = _provisioners.first()

    /**
     * THe next available network key index, or null if the index 4095 is already in use.
     *
     * Note: this method does not search for gaps in key indexes, takes next after the last one.
     */
    val nextAvailableNetworkKeyIndex: KeyIndex?
        get() {
            if (_networkKeys.isEmpty()) {
                return 0u
            }
            val nextKeyIndex = (_networkKeys.last().index + 1u).toUShort()
            if (nextKeyIndex.isValidKeyIndex()) {
                return nextKeyIndex
            }
            return null
        }

    /**
     * Returns the next available application key index that can be used
     * when construction an application key.
     */
    val nextAvailableApplicationKeyIndex: KeyIndex?
        get() {
            if (_applicationKeys.isEmpty()) {
                return 0u
            }
            val nextKeyIndex = (_applicationKeys.last().index + 1u).toUShort()
            if (nextKeyIndex.isValidKeyIndex()) {
                return nextKeyIndex
            }
            return null
        }

    /**
     * Updates timestamp to the current time in milliseconds.
     */
    internal fun updateTimestamp() {
        this.timestamp = Instant.fromEpochMilliseconds(System.currentTimeMillis())
    }

    /**
     * Checks if a provisioner with the given UUID already exists in the network.
     *
     * @param uuid UUID to check.
     * @return true if the provisioner exists.
     * @throws [DoesNotBelongToNetwork] if the provisioner belongs to another network.
     */
    internal fun hasProvisioner(uuid: UUID) = _provisioners.any { it.uuid == uuid }

    /**
     * Adds the given [Provisioner] to the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be added.
     * @throws [ProvisionerAlreadyExists] if the provisioner already exists.
     * @throws [DoesNotBelongToNetwork] if the provisioner does not belong to this network.
     * @throws [NoAddressesAvailable] if no address is available to be assigned.
     * @throws [OverlappingProvisionerRanges] if the given provisioner has any overlapping address
     * ranges with an existing provisioner.
     */
    @Throws(
        ProvisionerAlreadyExists::class,
        DoesNotBelongToNetwork::class,
        NoAddressesAvailable::class,
        OverlappingProvisionerRanges::class
    )
    fun add(provisioner: Provisioner) {
        add(
            provisioner = provisioner,
            address = nextAvailableUnicastAddress(
                elementCount = 1,
                provisioner = provisioner
            ) ?: throw NoAddressesAvailable()
        )
    }

    /**
     * Adds the given Provisioner with the given address to the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be added.
     * @throws [ProvisionerAlreadyExists] if the provisioner already exists.
     * @throws [DoesNotBelongToNetwork] if the provisioner does not belong to this network.
     * @throws [OverlappingProvisionerRanges] if the given provisioner has any overlapping address
     * ranges with an existing provisioner.
     */
    @Throws(OverlappingProvisionerRanges::class)
    fun add(provisioner: Provisioner, address: UnicastAddress?) {
        require(!_provisioners.contains(provisioner)) { throw ProvisionerAlreadyExists() }
        require(provisioner.network == null || provisioner.network == this) {
            throw DoesNotBelongToNetwork()
        }

        for (other in _provisioners) {
            require(!provisioner.hasOverlappingRanges(other)) {
                throw OverlappingProvisionerRanges()
            }
        }

        address?.apply {
            // Is the given address inside provisioner's address range?
            require(provisioner._allocatedUnicastRanges.any { it.contains(this.address) }) {
                throw AddressNotInAllocatedRanges()
            }
            // No other node uses the same address?
            require(!_nodes.any { it.containsElementWithAddress(this) }) { throw AddressAlreadyInUse() }
        }

        // Is it already added?
        require(!hasProvisioner(provisioner.uuid)) { return }

        // is there a node with the provisioner's uuid
        require(_nodes.none { it.uuid == provisioner.uuid }) { throw NodeAlreadyExists() }

        // Add the provisioner's node
        address?.let { unicastAddress ->
            val node = Node(
                provisioner,
                Crypto.generateRandomKey(),
                unicastAddress,
                listOf(
                    Element(
                        Location.UNKNOWN,
                        listOf(Model(SigModelId(Model.CONFIGURATION_SERVER_MODEL_ID)))
                    )
                ),
                _networkKeys,
                _applicationKeys
            ).apply {
                companyIdentifier = 0x00E0u //Google
                replayProtectionCount = maxUnicastAddress
            }
            add(node)
        }
        provisioner.network = this
        _provisioners.add(provisioner).also { updateTimestamp() }
        // TODO Needs to save the network
    }

    /**
     * Removes the provisioner at the given index.
     * Note: It is not possible to remove the last provisioner.
     *
     * @param index The position of the provisioner to be removed.
     * @return Provisioner that was removed.
     * @throws CannotRemove if there is only one provisioner.
     */
    @Throws(CannotRemove::class)
    internal fun removeProvisioner(index: Int): Provisioner {
        require(_provisioners.size > 1) { throw CannotRemove() }

        val localProvisionerRemoved = index == 0
        val provisioner = _provisioners[index]
        _provisioners.remove(provisioner)

        // If the old local Provisioner has been removed, and a new one has been set in it's place,
        // it needs the properties to be updated.
        if (localProvisionerRemoved) {
            _provisioners.first().node?.apply {
                netKeys = _networkKeys.map { NodeKey(it) }
                appKeys = _applicationKeys.map { NodeKey(it) }
                companyIdentifier = 0x00E0u
                replayProtectionCount = maxUnicastAddress
            }

            // TODO Save the local provisioner
        }
        updateTimestamp()
        return provisioner
    }

    /**
     * Removes the given provisioner from the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be removed.
     * @throws DoesNotBelongToNetwork if the the provisioner does not belong to this network.
     * @throws CannotRemove if there is only one provisioner.
     */
    @Throws(DoesNotBelongToNetwork::class, CannotRemove::class)
    fun remove(provisioner: Provisioner) {
        require(provisioner.network == this) { throw DoesNotBelongToNetwork() }
        removeProvisioner(_provisioners.indexOf(provisioner))
    }

    /**
     * Moves the provisioner from the given 'from' index to the specified 'to' index.
     *
     * @param from      Current index of the provisioner.
     * @param to        Destination index, the provisioner must be moved to.
     * @return Provisioner that was removed.
     * @throws DoesNotBelongToNetwork if the the provisioner does not belong to this network.
     * @throws CannotRemove if there is only one provisioner.
     */
    @Throws(DoesNotBelongToNetwork::class)
    internal fun moveProvisioner(from: Int, to: Int) {
        require(from >= 0 && from < _provisioners.size) {
            throw IllegalArgumentException("Invalid 'from' index!")
        }
        require(to >= 0 && to < _provisioners.size) {
            throw IllegalArgumentException("Invalid 'to' index!")
        }
        require(from != to) {
            return
        }
        _provisioners.add(to, removeProvisioner(from)).also { updateTimestamp() }
    }

    /**
     * Moves the given provisioner to the specified index.
     *
     * @param provisioner   Provisioner to be removed.
     * @param to            Destination index, the provisioner must be moved to.
     * @return Provisioner that was removed.
     * @throws DoesNotBelongToNetwork if the the provisioner does not belong to this network.
     * @throws CannotRemove if there is only one provisioner.
     */
    @Throws(DoesNotBelongToNetwork::class)
    fun move(provisioner: Provisioner, to: Int) {
        require(provisioner.network == this) { throw DoesNotBelongToNetwork() }
        _provisioners.indexOf(provisioner).takeIf { it > -1 }?.let { from ->
            moveProvisioner(from, to)
        }
    }

    /**
     * Disables the configuration capabilities by un-assigning provisioner's address. Un-assigning
     * an address will delete the provisioner's node. This results in the provisioner not being
     * able to send or receive mesh messages in the mesh network. However, the provisioner will
     * still retain it's provisioning capabilities.
     *
     * @param provisioner Provisioner of whose configurations are to be disabled.
     */
    fun disableConfigurationCapabilities(provisioner: Provisioner) {
        removeNode(provisioner.uuid)
    }

    /**
     * Adds the given [NetworkKey] to the list of network keys in the network.
     *
     * @param name      Network key name.
     * @param key       128-bit key to be added.
     * @param index     Network key index.
     * @throws [KeyIndexOutOfRange] if the key index is not within 0 - 4095.
     * @throws [DuplicateKeyIndex] if the key index is already in use.
     */
    @Throws(KeyIndexOutOfRange::class, DuplicateKeyIndex::class)
    fun add(name: String, key: ByteArray, index: KeyIndex? = null): NetworkKey {
        if (index != null) {
            // Check if the network key index is not already in use to avoid duplicates.
            require(_networkKeys.none { it.index == index }) { throw DuplicateKeyIndex() }
        }
        return NetworkKey(
            index = (index ?: nextAvailableNetworkKeyIndex) ?: throw KeyIndexOutOfRange(),
            _name = name,
            _key = key
        ).apply {
            this.network = this@MeshNetwork
        }.also { networkKey ->
            // Add the new network key to the network keys and sort them by index.
            _networkKeys.apply {
                add(networkKey)
            }.sortBy { it.index }
            updateTimestamp()
        }
    }

    /**
     * Removes a given [NetworkKey] from the list of network keys in the mesh network.
     *
     * @param key Network key to be removed.
     * @throws [DoesNotBelongToNetwork] if the key does not belong to this network.
     * @throws [KeyInUse] if the key is known to any node in the network or bound to any application
     *                    key in this network.
     */
    @Throws(DoesNotBelongToNetwork::class, KeyInUse::class)
    fun remove(key: NetworkKey) {
        require(key.network == this) { throw DoesNotBelongToNetwork() }
        require(!key.isInUse()) { throw KeyInUse() }
        _networkKeys.remove(key).also { updateTimestamp() }
    }

    /**
     * Adds the given [ApplicationKey] to the list of network keys in the network.
     *
     * @param name Application key name.
     * @param key 128-bit key to be added.
     * @param index Application key index.
     * @param boundNetworkKey Network key to which the application key must be bound to.
     * @throws [KeyIndexOutOfRange] if the key index is not within 0 - 4095.
     * @throws [DuplicateKeyIndex] if the key index is already in use.
     */
    @Throws(KeyIndexOutOfRange::class, DuplicateKeyIndex::class, IllegalArgumentException::class)
    fun add(
        name: String,
        key: ByteArray,
        index: KeyIndex? = null,
        boundNetworkKey: NetworkKey
    ): ApplicationKey {
        // Check if the network key belongs to the same network.
        require(boundNetworkKey.network == this) {
            throw IllegalArgumentException(
                "Network key ${boundNetworkKey.name} does not belong to network $name!"
            )
        }
        if (index != null) {
            // Check if the application key index is not already in use to avoid duplicates.
            require(_applicationKeys.none { it.index == index }) { throw DuplicateKeyIndex() }
        }
        return ApplicationKey(
            index = (index ?: nextAvailableNetworkKeyIndex) ?: throw KeyIndexOutOfRange(),
            _name = name,
            _key = key
        ).apply {
            this.boundNetKeyIndex = boundNetworkKey.index
            this.network = this@MeshNetwork
        }.also { applicationKey ->
            _applicationKeys.apply {
                add(applicationKey)
            }.sortBy { key -> key.index }
            updateTimestamp()
        }
    }

    /**
     * Removes a given [ApplicationKey] from the list of application keys in the mesh network.
     *
     * @param key Application key to be removed.
     * @throws [DoesNotBelongToNetwork] if the key does not belong to this network.
     * @throws [KeyInUse] if the key is known to any node in the network.
     */
    @Throws(DoesNotBelongToNetwork::class, KeyInUse::class)
    fun remove(key: ApplicationKey) {
        require(key.network == this) { throw DoesNotBelongToNetwork() }
        require(!key.isInUse()) { throw KeyInUse() }
        _applicationKeys.remove(key).also { updateTimestamp() }
    }

    /**
     * Returns the provisioner's node or null, if the provisioner is not a part of the network or
     * does not have an address assigned.
     *
     * @param provisioner Provisioner who's node is to be returned.
     * @return Null if the provisioner is not a part of the network or if the provisioner does not
     *         have an address assigned.
     */
    fun node(provisioner: Provisioner) = try {
        require(provisioner.network == this) { throw DoesNotBelongToNetwork() }
        require(hasProvisioner(provisioner.uuid)) { return null }
        node(provisioner.uuid)
    } catch (e: DoesNotBelongToNetwork) {
        null
    }

    /**
     * Returns the provisioned node for an unprovisioned device.
     *
     * @param device Unprovisioned node.
     * @return provisioned Node matching the unprovisioned device.
     */
    @Suppress("KDocUnresolvedReference")
    fun node(/*device:UnprovisionedDevice*/): Node? {
        TODO("return node(device.uuid)")
    }

    /**
     * Returns the node with the given uuid.
     *
     * @param uuid matching UUID.
     * @return Node
     */
    fun node(uuid: UUID) = nodes.find { it.uuid == uuid }

    /**
     * Adds a given [Node] to the list of nodes in the mesh network.
     *
     * @param node Node to be added to the network.
     * @throws NodeAlreadyExists if the node already exists.
     * @throws NoAddressesAvailable if the node is not assigned with an address.
     * @throws NoNetworkKey if the node does not contain a network key.
     * @throws DoesNotBelongToNetwork if the network key in the node does not match the keys in the
     *                                network.
     */
    @Throws(
        NodeAlreadyExists::class,
        NoAddressesAvailable::class,
        NoNetworkKey::class, DoesNotBelongToNetwork::class
    )
    internal fun add(node: Node) {
        // Ensure the node does not exists already.
        require(_nodes.none { it.uuid == node.uuid }) { throw NodeAlreadyExists() }
        // Verify if the address range is available for the new Node.
        require(isAddressAvailable(node.primaryUnicastAddress, node)) {
            throw NoAddressesAvailable()
        }
        // Ensure the Network Key exists.
        require(node.netKeys.isNotEmpty()) { throw NoNetworkKey() }
        // Make sure the network contains a Network Key with he same Key Index.
        require(_networkKeys.any { it.index == node.netKeys.first().index }) {
            throw DoesNotBelongToNetwork()
        }
        _nodes.add(node.also { it.network = this }).also { updateTimestamp() }
    }

    /**
     * Removes a given node from the list of nodes in the mesh network.
     *
     * @param node Node to be removed.
     */
    fun remove(node: Node) {
        removeNode(node.uuid)
    }

    /**
     * Removes a node with the given UUID from the mesh network.
     *
     * @param uuid UUID of the node to be removed.
     */
    internal fun removeNode(uuid: UUID) {
        _nodes.find {
            it.uuid == uuid
        }?.let { node ->
            _nodes.remove(node)
            // Remove unicast addresses of all node's elements from the scene
            _scenes.forEach { it.remove(node.addresses) }
            // When a Node is removed from the network, the unicast addresses that were in used
            // cannot be assigned to another node until the IV index is incremented by 2 which
            // effectively resets the Sequence number used by all the nodes in the network.
            _networkExclusions.add(ExclusionList(ivIndex.index).apply { exclude(node) })
        }.also { updateTimestamp() }
    }

    /**
     * Adds a given [Group] to the list of groups in the mesh network.
     *
     * @param group Group to be removed.
     * @throws [DoesNotBelongToNetwork] If the group does not belong to the network.
     * @throws [GroupAlreadyExists] If the group already exists.
     */
    @Throws(GroupAlreadyExists::class, DoesNotBelongToNetwork::class)
    fun add(group: Group) {
        require(!_groups.contains(group)) { throw GroupAlreadyExists() }
        require(group.network == null) { throw DoesNotBelongToNetwork() }
        _groups.add(
            group.also { it.network = this }
        ).also { updateTimestamp() }
    }

    /**
     * Removes a given [Group] from the list of groups in the mesh network.
     *
     * @param group Group to be removed.
     * @throws [DoesNotBelongToNetwork] If the group does not belong to the network.
     * @throws [GroupInUse] If the group is already in use.
     */
    @Throws(DoesNotBelongToNetwork::class, GroupInUse::class)
    fun remove(group: Group) {
        require(group.network == this) { throw DoesNotBelongToNetwork() }
        require(!group.isUsed) { throw GroupInUse() }
        _groups.remove(group).also { updateTimestamp() }
    }

    /**
     * Adds a given [Scene] to the list of scenes in the mesh network.
     *
     * @param scene Scene to be removed.
     * @throws [DoesNotBelongToNetwork] If the scene does not belong to the network.
     * @throws [SceneAlreadyExists] If the scene already exists.
     */
    @Throws(DoesNotBelongToNetwork::class, SceneAlreadyExists::class)
    fun add(scene: Scene) {
        require(!_scenes.contains(scene)) { throw SceneAlreadyExists() }
        require(scene.network == null) { throw DoesNotBelongToNetwork() }
        _scenes.add(
            scene.also { it.network = this }
        ).also { updateTimestamp() }
    }

    /**
     * Removes a given [Scene] from the list of groups in the mesh network.
     *
     * @param scene Scene to be removed.
     * @throws [DoesNotBelongToNetwork] If the scene does not belong to the network.
     * @throws [SceneInUse] If the scene is already in use.
     */
    @Throws(DoesNotBelongToNetwork::class)
    fun remove(scene: Scene) {
        require(scene.network == this) { throw DoesNotBelongToNetwork() }
        require(!scene.isUsed) { throw SceneInUse() }
        _scenes.remove(scene).also { updateTimestamp() }
    }

    /**
     * Checks if the address range is available for use.
     *
     * @param range Unicast range to check.
     * @return true if the given address range is available for use or false otherwise.
     */
    fun isAddressRangeAvailable(range: UnicastRange) = _nodes.none {
        it.containsElementsWithAddress(range)
    } && !_networkExclusions.contains(range, ivIndex)

    /**
     * Checks if the address is available to be assigned to a node with the given number of
     * elements or false otherwise.
     *
     * @param address         Possible address of the primary element of the node.
     * @param elementCount    Element count.
     * @return true if the address is available to be assigned to a node with given number of
     *         elements or false otherwise.
     */
    fun isAddressAvailable(address: UnicastAddress, elementCount: Int) = isAddressRangeAvailable(
        UnicastRange(address, elementCount)
    )

    /**
     * Checks if the address is available to be assigned to a node with the given number of
     * elements.
     *
     * @param address         Possible address of the primary element of the node.
     * @param node            Node
     * @return true if the address is assignable to the given node or false otherwise.
     */
    fun isAddressAvailable(address: UnicastAddress, node: Node): Boolean {
        val range = UnicastRange(address, (address + node.elementsCount))
        return nodes
            .filter { it.uuid != node.uuid }
            .none { it.containsElementsWithAddress(range) } && !_networkExclusions.contains(
            range,
            ivIndex
        )
    }

    /**
     * Returns the next available unicast address from the provisioner's range that can be assigned
     * to a new node based on the given number of elements. The zeroth element is identified by the
     * node's Unicast Address. Each following element is  identified by a subsequent Unicast
     * Address.
     *
     * @param elementCount Number of elements in the node.
     * @param provisioner  Provisioner that's provisioning the node.
     * @return the next available Unicast Address that can be assigned to the node or null if there
     *         are no addresses available in the allocated range.
     * @throws NoUnicastRangeAllocated if the provisioner has no address range allocated.
     */
    @Throws(NoUnicastRangeAllocated::class)
    fun nextAvailableUnicastAddress(elementCount: Int, provisioner: Provisioner): UnicastAddress? {
        require(provisioner._allocatedUnicastRanges.isNotEmpty()) {
            throw NoUnicastRangeAllocated()
        }
        val excludedAddresses = _networkExclusions
            .sortedBy { it.ivIndex }
            .flatMap { it._addresses }

        val addressesInUse = nodes
            .flatMap { it.elements }
            .map { it.unicastAddress }
            .sortedBy { it.address }

        val totalAddressesInUse = excludedAddresses + addressesInUse

        provisioner._allocatedUnicastRanges.forEach { range ->
            var address = range.lowAddress
            for (index in totalAddressesInUse.indices) {
                val usedAddress = totalAddressesInUse[index]

                // Skip nodes with addresses below the range.
                if (address > usedAddress) continue

                // If we found a space before the current node, return the address.
                if (usedAddress > address + (elementCount - 1)) return address

                // Else, move the address to the next available address.
                address = usedAddress + 1

                if (range.highAddress < address + (elementCount - 1)) break
            }
            if (range.highAddress >= address + (elementCount - 1)) return address
        }
        return null
    }

    /**
     * Returns the next available Group from the Provisioner's range that can be assigned to
     * a new Group.
     *
     * @param provisioner Provisioner, who's range is to be used for address generation.
     * @return The next available group address that can be assigned to a new Scene, or null, if
     *         there are no more available numbers in the allocated range.
     * @throws [NoGroupRangeAllocated] if no scene range is allocated to the provisioner.
     */
    @Throws(NoGroupRangeAllocated::class)
    fun nextAvailableGroup(provisioner: Provisioner): GroupAddress? {
        require(provisioner._allocatedGroupRanges.isNotEmpty()) { throw NoGroupRangeAllocated() }
        val sortedGroups = _groups.sortedBy { it.address.address }

        // Iterate through all scenes just once, while iterating over ranges.
        var index = 0
        provisioner._allocatedGroupRanges.forEach { groupRange ->
            var groupAddress = groupRange.lowAddress

            // Iterate through scene objects that weren't checked yet.
            val currentIndex = index
            for (i in currentIndex until sortedGroups.size) {
                val group = sortedGroups[i]
                index += 1
                // Skip scenes with number below the range.
                if (groupAddress > group.address) continue

                // If we found a space before the current node, return the scene number.
                if (groupAddress < group.address) return groupAddress

                // Else, move the address to the next available address.
                groupAddress = (group.address as GroupAddress) + 1

                // If the new scene number is outside of the range, go to the next one.
                if (groupAddress > groupRange.highAddress) break
            }

            // If the range has available space, return the address.
            if (groupAddress <= groupRange.highAddress) return groupAddress
        }
        // No group address was found :(
        return null
    }

    /**
     * Returns the next available Scene number from the Provisioner's range that can be assigned to
     * a new Scene.
     *
     * @param provisioner Provisioner, who's range is to be used for address generation.
     * @return The next available Scene number that can be assigned to a new Scene, or null, if
     *         there are no more available numbers in the allocated range.
     * @throws [NoSceneRangeAllocated] if no scene range is allocated to the provisioner.
     */
    @Throws(NoSceneRangeAllocated::class)
    fun nextAvailableScene(provisioner: Provisioner): SceneNumber? {
        require(provisioner._allocatedSceneRanges.isNotEmpty()) { throw NoSceneRangeAllocated() }
        val sortedScenes = _scenes.sortedBy { it.number }

        // Iterate through all scenes just once, while iterating over ranges.
        var index = 0
        provisioner._allocatedSceneRanges.forEach { range ->
            var scene = range.firstScene

            // Iterate through scene objects that weren't checked yet.
            val currentIndex = index
            for (i in currentIndex until sortedScenes.size) {
                val sceneObject = sortedScenes[i]
                index += 1
                // Skip scenes with number below the range.
                if (scene > sceneObject.number) continue

                // If we found a space before the current node, return the scene number.
                if (scene < sceneObject.number) return scene

                // Else, move the address to the next available address.
                scene = (sceneObject.number + 1u).toUShort()

                // If the new scene number is outside of the range, go to the next one.
                if (scene > range.lastScene) break
            }

            // If the range has available space, return the address.
            if (scene <= range.lastScene) return scene
        }
        // No scene number was found :(
        return null
    }

    internal companion object {
        /**
         *  Invoked when an observable property is changed.
         *
         *  @param oldValue Old value of the property.
         *  @param newValue New value to be assigned.
         *  @param action Lambda to be invoked if the [newValue] is not the same as [oldValue].
         */
        internal fun <T> onChange(oldValue: T, newValue: T, action: () -> Unit) {
            if (newValue != oldValue)
                action()
        }
    }
}

