@file:Suppress("UNUSED_PARAMETER")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import no.nordicsemi.kotlin.mesh.core.NetworkPropertiesStorage
import java.util.UUID

/**
 * Mocked for tests.
 */
internal class TestPropertiesStorage : NetworkPropertiesStorage {
    override val sequenceNumbers: MutableMap<UnicastAddress, UInt>
        get() = TODO("Not yet implemented")
    override var ivIndex: IvIndex
        get() = TODO("Not yet implemented")
        set(value) {}
    override var lastTransitionDate: Instant
        get() = TODO("Not yet implemented")
        set(value) {}
    override var isIvRecoveryActive: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun load(scope: CoroutineScope, uuid: UUID, addresses: List<UnicastAddress>) {
        TODO("Not yet implemented")
    }

    override suspend fun save(uuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun lastSeqAuthValue(source: Address): Flow<ULong?> {
        TODO("Not yet implemented")
    }

    override suspend fun storeLastSeqAuthValue(lastSeqAuth: ULong, source: Address) {
        TODO("Not yet implemented")
    }

    override fun previousSeqAuthValue(source: Address): Flow<ULong?> {
        TODO("Not yet implemented")
    }

    override suspend fun storePreviousSeqAuthValue(seqAuth: ULong, source: Address) {
        TODO("Not yet implemented")
    }

    override suspend fun removeSeqAuthValues(node: Node) {
        TODO("Not yet implemented")
    }

    override suspend fun removeSeqAuthValues(source: Address) {
        TODO("Not yet implemented")
    }
}