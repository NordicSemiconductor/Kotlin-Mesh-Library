@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.deserialize
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer.serialize

open class MeshNetworkManager {
    lateinit var meshNetwork: MeshNetwork
        protected set

    /**
     * Imports a MeshNetwork from using a Json defined by the Mesh Configuration Database Profile.
     */
    // TODO Should we import a Json Object by default?
    suspend fun importMeshNetwork(array: ByteArray) {
        meshNetwork = deserialize(array)
        // Assign network reference to access parent network within the object.
        meshNetwork.apply {
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
            _provisioners.forEach {
                it.network = this
            }
            nodes.forEach { node ->
                node.network = this
                node.elements.forEach { element ->
                    element.parentNode = node
                    element.models.forEach { model ->
                        model.parentElement = element
                    }
                }
            }
            networkExclusions.forEach {
                it.network = this
            }
        }
    }

    /**
     * Exports a mesh network to a Json defined by the Mesh Configuration Database Profile.
     */
    suspend fun exportMeshNetwork() = serialize(network = meshNetwork)
}