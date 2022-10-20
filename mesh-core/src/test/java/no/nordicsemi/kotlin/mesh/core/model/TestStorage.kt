package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.kotlin.mesh.core.Storage
import java.util.*

/**
 * Mocked for tests.
 */
internal class TestStorage : Storage {

    override suspend fun load(): Flow<ByteArray> {
        TODO("Do nothing")
    }

    override suspend fun save(uuid: UUID, network: String) {
        TODO("Do nothing")
    }
}