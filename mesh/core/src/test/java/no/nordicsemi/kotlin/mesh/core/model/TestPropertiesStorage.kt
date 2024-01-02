package no.nordicsemi.kotlin.mesh.core.model

import no.nordicsemi.kotlin.mesh.core.SecurePropertiesStorage
import java.util.UUID

/**
 * Mocked for tests.
 */
internal class TestPropertiesStorage : SecurePropertiesStorage {
    override suspend fun ivIndex(uuid: UUID): IvIndex {
        TODO("Not yet implemented")
    }

    override suspend fun storeIvIndex(uuid: UUID, ivIndex: IvIndex) {
        TODO("Not yet implemented")
    }

    override suspend fun nextSequenceNumber(uuid: UUID, address: UnicastAddress): UInt {
        TODO("Not yet implemented")
    }

    override suspend fun storeNextSequenceNumber(
        uuid: UUID,
        address: UnicastAddress,
        sequenceNumber: UInt
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun resetSequenceNumber(uuid: UUID, address: UnicastAddress) {
        TODO("Not yet implemented")
    }

    override suspend fun lastSeqAuthValue(uuid: UUID, source: UnicastAddress): ULong? {
        TODO("Not yet implemented")
    }

    override suspend fun storeLastSeqAuthValue(
        uuid: UUID,
        source: UnicastAddress,
        lastSeqAuth: ULong
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun previousSeqAuthValue(uuid: UUID, source: UnicastAddress): ULong? {
        TODO("Not yet implemented")
    }

    override suspend fun storePreviousSeqAuthValue(
        uuid: UUID,
        source: UnicastAddress,
        seqAuth: ULong
    ) {
        TODO("Not yet implemented")
    }
}