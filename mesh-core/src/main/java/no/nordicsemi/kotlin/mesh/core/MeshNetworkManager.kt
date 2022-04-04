@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import kotlinx.serialization.ExperimentalSerializationApi
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize

open class MeshNetworkManager internal constructor() {
    lateinit var meshNetwork: MeshNetwork
        protected set

    /**
     * Imports a MeshNetwork from using a Json defined by the Mesh Configuration Database Profile.
     */
    @OptIn(ExperimentalSerializationApi::class)
    // TODO Should we import a Json Object by default?
    suspend fun importMeshNetwork(array: ByteArray) {
        meshNetwork = deserialize(array)
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile.
     */
    suspend fun exportMeshNetwork() = serialize(network = meshNetwork)
}