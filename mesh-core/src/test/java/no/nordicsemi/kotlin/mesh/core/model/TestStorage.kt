package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import no.nordicsemi.kotlin.mesh.core.LocalStorage
import java.util.*

/**
 * Mocked for tests.
 */
internal class TestStorage : LocalStorage {
    override val dataStream: Flow<ByteArray>
        get() = flow { emit(byteArrayOf()) }

    override suspend fun save(uuid: UUID, network: String) {
        TODO("Do nothing")
    }
}