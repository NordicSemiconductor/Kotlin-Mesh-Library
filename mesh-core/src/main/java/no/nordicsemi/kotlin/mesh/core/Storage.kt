package no.nordicsemi.kotlin.mesh.core

import java.util.*

/**
 * Allows the users to save the mesh network information in a custom location.
 */
interface Storage {

    /**
     * Loads the Mesh Network from a user specified storage.
     */
    suspend fun load(): ByteArray?

    /**
     * Saves the Mesh Network in a user specified storage.
     */
    suspend fun save(uuid: UUID, network: ByteArray)
}