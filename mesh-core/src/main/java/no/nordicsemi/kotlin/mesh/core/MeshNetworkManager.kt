@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize
import java.util.*

open class MeshNetworkManager {
    lateinit var meshNetwork: MeshNetwork
        protected set

    /**
     * Creates a Mesh Network with a given name and a UUID. If a UUID is not provided a random will
     * be generated.
     *
     * @param name Name of the mesh network.
     * @param uuid 128-bit Universally Unique Identifier (UUID), which allows differentiation among
     *             multiple mesh networks.
     */
    fun createMeshNetwork(name: String, uuid: UUID = UUID.randomUUID()): MeshNetwork {
        return MeshNetwork(uuid = uuid, _name = name).also {
            meshNetwork = it
            // TODO save the network may be?
        }
    }

    /**
     * Imports a MeshNetwork from a byte array containing a Json defined by the Mesh Configuration
     * Database profile.
     *
     * @return a mesh network configuration decoded from the given byte array.
     * @throws ImportError if deserializing fails.
     */
    @Throws(ImportError::class)
    suspend fun importMeshNetwork(array: ByteArray): MeshNetwork = run {
        deserialize(array).also {
            meshNetwork = it
        }
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile.
     */
    suspend fun exportMeshNetwork() = serialize(network = meshNetwork)
}