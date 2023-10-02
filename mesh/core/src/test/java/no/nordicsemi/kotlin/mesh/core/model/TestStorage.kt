package no.nordicsemi.kotlin.mesh.core.model

import no.nordicsemi.kotlin.mesh.core.Storage

/**
 * Mocked for tests.
 */
internal class TestStorage : Storage {

    override suspend fun load(): ByteArray {
        TODO("Do nothing")
    }

    override suspend fun save(network: ByteArray) {
        TODO("Not yet implemented")
    }
}