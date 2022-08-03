@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.*
import java.util.*

/**
 * MeshNetworkManager is the entry point to the Mesh library.
 *
 * @param storage Custom storage option allowing users to save the mesh network to a custom
 *                location.
 */
class MeshNetworkManager(private val storage: LocalStorage) {
    internal lateinit var meshNetwork: MeshNetwork

    private val _network: MutableSharedFlow<MeshNetwork> = MutableSharedFlow(replay = 1)
    val network: Flow<MeshNetwork> = _network.asSharedFlow()

    /**
     * Loads the network from the storage provided by the user.
     * @return true if the configuration was successfully loaded or false otherwise.
     */
    suspend fun load(): Boolean {
        return storage.load().first().takeIf {
            it.isNotEmpty()
        }?.let {
            meshNetwork = deserialize(it)
            _network.emit(meshNetwork)
            true
        } ?: false
    }

    /**
     * Saves the network in the local storage provided by the user.
     */
    suspend fun save() {
        storage.save(uuid = meshNetwork.uuid, network = export().toString())
    }

    /**
     * Creates a Mesh Network with a given name and a UUID. If a UUID is not provided a random will
     * be generated.
     *
     * @param name Name of the mesh network.
     * @param uuid 128-bit Universally Unique Identifier (UUID), which allows differentiation among
     *             multiple mesh networks.
     */
    fun create(name: String, uuid: UUID = UUID.randomUUID()): MeshNetwork {
        return MeshNetwork(uuid = uuid, _name = name).also {
            meshNetwork = it
        }
    }

    /**
     * Imports a Mesh Network from a byte array containing a Json defined by the Mesh Configuration
     * Database profile.
     *
     * @return a mesh network configuration decoded from the given byte array.
     * @throws ImportError if deserializing fails.
     */
    @Throws(ImportError::class)
    suspend fun import(array: ByteArray): MeshNetwork = run {
        deserialize(array).also {
            _network.emit(it)
            meshNetwork = it
            save()
        }
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile.
     */
    suspend fun export() = serialize(network = meshNetwork)

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile based
     * on the given configuration.
     *
     * @param networkKeysConfig            Configuration of the network keys to be exported.
     * @param applicationKeysConfig        Configuration of the application keys to be exported.
     * @param provisionersConfig           Configuration of the provisioner to be exported.
     * @param nodesConfig                  Configuration of the nodes to be exported.
     * @param groupConfig                  Configuration of the groups to be exported.
     * @param scenesConfig                 Configuration of the scenes to be exported.
     */
    suspend fun export(
        networkKeysConfig: NetworkKeysConfig,
        applicationKeysConfig: ApplicationKeysConfig,
        provisionersConfig: ProvisionersConfig,
        nodesConfig: NodesConfig,
        groupConfig: GroupsConfig,
        scenesConfig: ScenesConfig
    ) = serialize(
        network = meshNetwork,
        networkKeysConfig = networkKeysConfig,
        applicationKeysConfig = applicationKeysConfig,
        provisionersConfig = provisionersConfig,
        nodesConfig = nodesConfig,
        scenesConfig = scenesConfig,
        groupsConfig = groupConfig
    )
}
