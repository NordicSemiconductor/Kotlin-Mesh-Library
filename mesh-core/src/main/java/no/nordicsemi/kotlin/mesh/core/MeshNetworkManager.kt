@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import no.nordicsemi.kotlin.mesh.core.exception.ImportError
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize

open class MeshNetworkManager {
    lateinit var meshNetwork: MeshNetwork
        protected set

    /**
     * Imports a MeshNetwork from using a Json defined by the Mesh Configuration Database Profile.
     *
     * @return Returns a MeshNetwork.
     * @throws ImportError in deserializing fails.
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