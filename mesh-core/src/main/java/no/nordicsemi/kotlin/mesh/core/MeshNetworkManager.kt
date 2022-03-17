@file:Suppress("unused", "RedundantSuspendModifier")

package no.nordicsemi.kotlin.mesh.core

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.MeshNetworkSerializer

open class MeshNetworkManager internal constructor() {
    lateinit var meshNetwork: MeshNetwork
        protected set

    @OptIn(ExperimentalSerializationApi::class)
    suspend fun importMeshNetwork(array: ByteArray) {
        meshNetwork = MeshNetworkSerializer.deserialize(array)
    }

    suspend fun exportMeshNetwork(): JsonElement {
        return MeshNetworkSerializer.serialize(network = meshNetwork)
    }
}