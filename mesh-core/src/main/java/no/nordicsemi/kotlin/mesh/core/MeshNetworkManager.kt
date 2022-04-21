@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import no.nordicsemi.android.mesh.storage.Storage
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize

open class MeshNetworkManager internal constructor() {
    lateinit var meshNetwork: MeshNetwork
        protected set

    /**
     * Imports a MeshNetwork from using a Json defined by the Mesh Configuration Database Profile.
     */
    // TODO Should we import a Json Object by default?
    suspend fun importMeshNetwork(array: ByteArray) {
        meshNetwork = deserialize(array)
        meshNetwork.apply {
            // Assign network reference to improve api.
            networkKeys.forEach {
                it.network = this
            }
            applicationKeys.forEach {
                it.network = this
            }
            groups.forEach {
                it.network = this
            }
            scenes.forEach {
                it.network = this
            }
            nodes.forEach { node ->
                node.network = this
                // Assigns parent node reference to improve api.
                node.elements.forEach { element ->
                    element.parentNode = node
                    // Assigns parent element reference to improve api.
                    element.models.forEach { model ->
                        model.parentElement = element
                    }
                }
            }
        }
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile.
     */
    suspend fun exportMeshNetwork() = serialize(network = meshNetwork)
}