@file:Suppress("unused", "RedundantSuspendModifier")
@file:OptIn(ExperimentalCoroutinesApi::class)

package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonObject
import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.NetworkConfiguration
import java.util.*

/**
 * MeshNetworkManager is the entry point to the Mesh library.
 *
 * @param storage Custom storage option allowing users to save the mesh network to a custom
 *                location.
 */
class MeshNetworkManager(private val storage: LocalStorage) {
    internal lateinit var meshNetwork: MeshNetwork

    private val _network: MutableSharedFlow<MeshNetwork> = MutableSharedFlow(
        replay = 1,
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val network: Flow<MeshNetwork> = _network.asSharedFlow()

    /**
     * Loads the network from the storage provided by the user.
     * @return true if the configuration was successfully loaded or false otherwise.
     */
    suspend fun load() = storage.dataStream.first().takeIf {
        it.isNotEmpty()
    }?.let {
        meshNetwork = deserialize(it)
        _network.emit(meshNetwork)
        true
    } ?: false

    /**
     * Saves the network in the local storage provided by the user.
     */
    suspend fun save() {
        storage.save(uuid = meshNetwork.uuid, network = exportNetwork().toString())
        _network.emit(meshNetwork)
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
            //_network.emit(it)
            meshNetwork = it
            save()
        }
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile based
     * on the given configuration.
     *
     * @param configuration Specifies if the network should be fully exported or partially.
     * @return Bytearray containing the Mesh network configuration.
     */
    suspend fun export(
        configuration: NetworkConfiguration = NetworkConfiguration.Full
    ): ByteArray {
        return exportNetwork(
            configuration = configuration
        ).toString().toByteArray()
    }

    /**
     * Internal api that Exports a mesh network to a Json defined by the Mesh Configuration Database
     * Profile based on the given configuration.
     *
     * @param configuration Specifies if the network should be fully exported or partially.
     * @return JsonObject containing the mesh network configuration.
     */
    private fun exportNetwork(
        configuration: NetworkConfiguration = NetworkConfiguration.Full
    ): JsonObject {
        val network = meshNetwork
        return serialize(
            network = network,
            configuration = configuration
        )
    }
}
