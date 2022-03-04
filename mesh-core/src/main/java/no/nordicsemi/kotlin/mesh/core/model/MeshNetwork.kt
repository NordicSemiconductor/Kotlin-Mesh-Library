@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.TimestampSerializer
import no.nordicsemi.kotlin.mesh.core.model.serialization.UuidSerializer
import java.util.*

/**
 * MeshNetwork representing a bluetooth mesh network
 *
 * @property meshUUID               128-bit Universally Unique Identifier (UUID), which allows differentiation among multiple mesh networks.
 * @property meshName               Human-readable name for the mesh network.
 * @property timestamp              Represents the last time the Mesh Object has been modified. The timestamp is based on Coordinated Universal Time.
 * @property partial                Indicates if this Mesh Configuration Database is part of a larger database.
 * @property networkKeys            List of network keys that includes information about network keys used in the mesh network.
 * @property applicationKeys        List of app keys that includes information about app keys used in the mesh network.
 * @property provisioners           List of known Provisioners and ranges of addresses that have been allocated to these Provisioners.
 * @property nodes                  List of nodes that includes information about mesh nodes in the mesh network.
 * @property groups                 List of groups that includes information about groups configured in the mesh network.
 * @property scenes                 List of scenes that includes information about scenes configured in the mesh network.
 * @property networkExclusions      List of [ExclusionList]
 *
 */
@Serializable
class MeshNetwork(@Serializable(with = UuidSerializer::class) val meshUUID: UUID = UUID.randomUUID()) {

    @SerialName(value = "\$schema")
    internal var schema = "http://json-schema.org/draft-04/schema#"
    internal var id =
        "http://www.bluetooth.com/specifications/assigned-numbers/meshprofile/cdb-schema.json#"
    internal var version = "1.0.0"

    var meshName: String = "Mesh Network"
        set(value) {
            require(meshName.isNotBlank()) { "Network name cannot be empty!" }
            field = value
            updateTimestamp()
        }

    @Serializable(with = TimestampSerializer::class)
    var timestamp: Long = System.currentTimeMillis()
        private set

    var partial: Boolean = false
        set(value) {
            field = value
            updateTimestamp()
        }

    var provisioners = listOf<Provisioner>()
        private set

    @SerialName("netKeys")
    var networkKeys = listOf<NetworkKey>()
        private set

    @SerialName("appKeys")
    var applicationKeys = listOf<ApplicationKey>()
        private set

    var nodes: List<Node> = listOf()
        private set

    var groups: List<Group> = listOf()
        private set

    var scenes: List<Scene> = listOf()
        private set

    var networkExclusions = listOf<ExclusionList>()
        private set

    /**
     * Updates timestamp to the current time in milliseconds.
     */
    internal fun updateTimestamp() {
        this.timestamp = System.currentTimeMillis()
    }

    /**
     * Adds the given [Provisioner] to the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be added.
     */
    fun addProvisioner(provisioner: Provisioner) {
        provisioners = provisioners + provisioner
        updateTimestamp()
    }

    /**
     * Removes the given [Provisioner] from the list of provisioners in the network.
     *
     * @param provisioner Provisioner to be added.
     */
    fun removeProvisioner(provisioner: Provisioner) {
        provisioners = provisioners - provisioner
        updateTimestamp()
    }

    /**
     * Creates a network key.
     *
     * @return Network key
     */
    fun createNetworkKey(): NetworkKey {
        TODO(reason = "Not implemented yet")
    }

    /**
     * Adds the given [NetworkKey] to the list of network keys in the network.
     *
     * @param networkKey Network key to be added.
     */
    fun addNetworkKey(networkKey: NetworkKey) {
        networkKeys = networkKeys + networkKey
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [NetworkKey] from the list of network keys in the mesh network.
     *
     * @param networkKey Network key to be removed.
     */
    fun removeNetworkKey(networkKey: NetworkKey) {
        networkKeys = networkKeys - networkKey
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Adds a given [Node] to the list of nodes in the mesh network.
     *
     * @param node Node to be removed.
     */
    internal fun addNode(node: Node) {
        nodes = nodes + node
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [Node] from the list of nodes in the mesh network.
     *
     * @param node Node to be removed.
     */
    fun removeNode(node: Node) {
        nodes = nodes - node
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Adds a given [Group] to the list of groups in the mesh network.
     *
     * @param group Group to be removed.
     */
    internal fun addGroup(group: Group) {
        groups = groups + group
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [Group] from the list of groups in the mesh network.
     *
     * @param group Group to be removed.
     */
    fun removeGroup(group: Group) {
        groups = groups - group
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Adds a given [Group] to the list of groups in the mesh network.
     *
     * @param scene Group to be removed.
     */
    internal fun addScene(scene: Scene) {
        scenes = scenes + scene
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }

    /**
     * Removes a given [Group] from the list of groups in the mesh network.
     *
     * @param scene Group to be removed.
     */
    fun removeScene(scene: Scene) {
        scenes = scenes - scene
        updateTimestamp()
        TODO(reason = "Implementation incomplete")
    }
}