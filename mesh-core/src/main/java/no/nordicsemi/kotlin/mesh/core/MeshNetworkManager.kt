@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.flow.*
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
class MeshNetworkManager(private val storage: Storage) {

    private val _meshNetwork = MutableSharedFlow<MeshNetwork>(replay = 1, extraBufferCapacity = 10)
    val meshNetwork = _meshNetwork.asSharedFlow()
    private lateinit var network: MeshNetwork

    /**
     * Loads the network from the storage provided by the user.
     *
     * @return true if the configuration was successfully loaded or false otherwise.
     */
    suspend fun load() = storage.load()?.takeIf { it.isNotEmpty() }?.let {
        val meshNetwork = deserialize(it)
        this@MeshNetworkManager.network = meshNetwork
        _meshNetwork.emit(meshNetwork)
        true
    } ?: false

    /**
     * Saves the network in the local storage provided by the user.
     */
    suspend fun save() {
        export().also {
            val meshNetwork = this@MeshNetworkManager.network
            storage.save(meshNetwork.uuid, it)
            _meshNetwork.emit(meshNetwork)
        }
    }

    /**
     * Creates a Mesh Network with a given name and a UUID. If a UUID is not provided a random will
     * be generated.
     *
     * @param name Name of the mesh network.
     * @param uuid 128-bit Universally Unique Identifier (UUID), which allows differentiation among
     *             multiple mesh networks.
     */
    suspend fun create(name: String = "Mesh Network", uuid: UUID = UUID.randomUUID()) =
        MeshNetwork(uuid = uuid, _name = name).also {
            network = it
            _meshNetwork.emit(it)
        }

    /**
     * Imports a Mesh Network from a byte array containing a Json defined by the Mesh Configuration
     * Database profile.
     *
     * @return a mesh network configuration decoded from the given byte array.
     * @throws ImportError if deserializing fails.
     */
    @Throws(ImportError::class)
    suspend fun import(array: ByteArray) =
        deserialize(array).also {
            network = it
            _meshNetwork.emit(it)
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
    ) = network.let {
        serialize(
            network = it,
            configuration = configuration
        ).toString().toByteArray()
    }
}

