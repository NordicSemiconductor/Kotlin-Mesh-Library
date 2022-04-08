@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.exceptions.DoesNotBelongToNetwork
import no.nordicsemi.kotlin.mesh.core.exceptions.DuplicateKeyIndex
import no.nordicsemi.kotlin.mesh.core.exceptions.KeyInUse
import no.nordicsemi.kotlin.mesh.core.exceptions.KeyIndexOutOfRange
import no.nordicsemi.kotlin.mesh.core.model.serialization.UUIDSerializer
import java.util.*
import kotlin.properties.Delegates

/**
 * MeshNetwork representing a Bluetooth mesh network.
 *
 * @property uuid                   128-bit Universally Unique Identifier (UUID), which allows differentiation among multiple mesh networks.
 * @property name                   Human-readable name for the mesh network.
 * @property timestamp              Represents the last time the Mesh Object has been modified. The timestamp is based on Coordinated Universal Time.
 * @property partial                Indicates if this Mesh Configuration Database is part of a larger database.
 * @property networkKeys            List of network keys that includes information about network keys used in the mesh network.
 * @property applicationKeys        List of app keys that includes information about app keys used in the mesh network.
 * @property provisioners           List of known Provisioners and ranges of addresses that have been allocated to these Provisioners.
 * @property nodes                  List of nodes that includes information about mesh nodes in the mesh network.
 * @property groups                 List of groups that includes information about groups configured in the mesh network.
 * @property scenes                 List of scenes that includes information about scenes configured in the mesh network.
 * @property networkExclusions      List of [ExclusionList].
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
            onChange(oldValue = _name, newValue = value, action = { updateTimestamp() })
            _name = value
        }
    var timestamp: Instant = Instant.fromEpochMilliseconds(System.currentTimeMillis())
        internal set

    @Suppress("RedundantSetter")
    var partial: Boolean by Delegates.observable(initialValue = false) { _, oldValue, newValue ->
        onChange(oldValue = oldValue, newValue = newValue, action = { updateTimestamp() }
        )
    }

    var provisioners: List<Provisioner> = listOf()
        private set

    @SerialName("netKeys")
    var networkKeys: List<NetworkKey> = listOf()
        private set

    @SerialName("appKeys")
    var applicationKeys: List<ApplicationKey> = listOf()
        private set

    var nodes: List<Node> = listOf()
        private set

    var groups: List<Group> = listOf()
        private set

    var scenes: List<Scene> = listOf()
        private set

    var networkExclusions: List<ExclusionList> = listOf()
        private set

    /**
     * THe next available network key index, or null if the index 4095 is already in use.
     *
     * Note: this method does not search for gaps in key indexes, just tooks next after the last one.
     */
    val nextAvailableNetworkKeyIndex: KeyIndex?
        get() {
            if (networkKeys.isEmpty()) {
                return 0u
            }
            val nextKeyIndex = (networkKeys.last().index + 1u).toUShort()
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
            if (applicationKeys.isEmpty()) {
                return 0u
            }
            val nextKeyIndex = (applicationKeys.last().index + 1u).toUShort()
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
     * Adds the given [Provisioner] to the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be added.
     * @return True if the provisioner was added or false because the provisioner already exists.
     */
    fun add(provisioner: Provisioner) = when {
        provisioners.contains(provisioner) -> false
        else -> {
            provisioners = provisioners + provisioner
            updateTimestamp()
            TODO("Implement like in iOS")
        }
    }

    /**
     * Removes the given provisioner from the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be removed.
     */
    fun remove(provisioner: Provisioner) {
        require(provisioner.network == this) { throw DoesNotBelongToNetwork() }
        if (provisioners.contains(provisioner)) {
            provisioners = provisioners - provisioner
            updateTimestamp()
        }
    }

    /**
     * Adds the given [NetworkKey] to the list of network keys in the network.
     *
     * @param name Network key name.
     * @param key 128-bit key to be added.
     * @param index Network key index.
     * @throws [KeyIndexOutOfRange] if the key index is not within 0 - 4095.
     * @throws [DuplicateKeyIndex] if the key index is already in use.
     */
    @Throws(KeyIndexOutOfRange::class, DuplicateKeyIndex::class)
    fun add(
        name: String,
        key: ByteArray,
        index: KeyIndex? = null
    ): NetworkKey {
        if (index != null) {
            // Check if the network key index is not already in use to avoid duplicates.
            require(networkKeys.none { it.index == index }) { throw DuplicateKeyIndex() }
        }
        return NetworkKey(
            index = (index ?: nextAvailableNetworkKeyIndex) ?: throw KeyIndexOutOfRange(),
            _name = name,
            _key = key
        ).apply {
            this.network = this@MeshNetwork
        }.also { networkKey ->
            // Add the new network key to the network keys and sort them by index.
            networkKeys = (networkKeys + networkKey).sortedBy { it.index }
            updateTimestamp()
        }
    }

    /**
     * Removes a given [NetworkKey] from the list of network keys in the mesh network.
     *
     * @param key Network key to be removed.
     * @throws [DoesNotBelongToNetwork] if the key does not belong to this network.
     * @throws [KeyInUse] if the key is known to any node in the network or bound to any application key in this network.
     */
    @Throws(DoesNotBelongToNetwork::class, KeyInUse::class)
    fun remove(key: NetworkKey) {
        require(key.network == this) { throw DoesNotBelongToNetwork() }
        require(!key.isInUse()) { throw KeyInUse() }
        networkKeys = networkKeys - key
        updateTimestamp()
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
        boundNetworkKey: NetworkKey,
    ): ApplicationKey {
        // Check if the network key belongs to the same network.
        require(boundNetworkKey.network == this) {
            throw IllegalArgumentException("Network key ${boundNetworkKey.name} does not belong to network $name!")
        }
        if (index != null) {
            // Check if the application key index is not already in use to avoid duplicates.
            require(applicationKeys.none { it.index == index }) { throw DuplicateKeyIndex() }
        }
        return ApplicationKey(
            index = (index ?: nextAvailableNetworkKeyIndex) ?: throw KeyIndexOutOfRange(),
            _name = name,
            _key = key
        ).apply {
            this.boundNetKeyIndex = boundNetworkKey.index
            this.network = this@MeshNetwork
        }.also { applicationKey ->
            applicationKeys = (applicationKeys + applicationKey).sortedBy { it.index }
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
        applicationKeys = applicationKeys - key
        updateTimestamp()
    }

    /**
     * Adds a given [Node] to the list of nodes in the mesh network.
     *
     * @param node Node to be removed.
     */
    internal fun add(node: Node) {
        nodes = nodes + node
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [Node] from the list of nodes in the mesh network.
     *
     * @param node Node to be removed.
     */
    fun remove(node: Node) {
        nodes = nodes - node
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Adds a given [Group] to the list of groups in the mesh network.
     *
     * @param group Group to be removed.
     */
    internal fun add(group: Group) {
        groups = groups + group
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [Group] from the list of groups in the mesh network.
     *
     * @param group Group to be removed.
     */
    fun remove(group: Group) {
        groups = groups - group
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Adds a given [Group] to the list of groups in the mesh network.
     *
     * @param scene Group to be removed.
     */
    internal fun add(scene: Scene) {
        scenes = scenes + scene
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [Group] from the list of groups in the mesh network.
     *
     * @param scene Group to be removed.
     */
    fun remove(scene: Scene) {
        scenes = scenes - scene
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    companion object {
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