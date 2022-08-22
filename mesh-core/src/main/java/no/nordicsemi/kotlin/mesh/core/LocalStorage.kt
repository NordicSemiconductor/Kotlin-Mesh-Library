package no.nordicsemi.kotlin.mesh.core

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonObject
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.util.*

/**
 * Allows the users to save the mesh network information in a custom location.
 */
interface LocalStorage {

    val dataStream:Flow<ByteArray>

    /**
     * Saves the Mesh Network in a user specified local storage.
     */
    suspend fun load(): Flow<ByteArray>

    /**
     * Saves the Mesh Network in a user specified local storage.
     */
    suspend fun save(uuid: UUID, network: String): ByteArray?
}