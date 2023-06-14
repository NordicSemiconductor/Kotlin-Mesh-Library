package no.nordicsemi.kotlin.mesh.core.model

import no.nordicsemi.kotlin.mesh.core.Storage
import java.util.UUID

/**
 * Mocked for tests.
 */
internal class TestStorage : Storage {

    override suspend fun load(): ByteArray? {
        TODO("Do nothing")
    }

    override suspend fun save(uuid: UUID, network: ByteArray) {
        TODO("Do nothing")
    }
}