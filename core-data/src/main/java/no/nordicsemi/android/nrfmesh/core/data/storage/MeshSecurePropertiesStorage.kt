@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.data.storage

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.android.nrfmesh.core.data.ProtoIvIndex
import no.nordicsemi.android.nrfmesh.core.data.ProtoSecurePropertiesMap
import no.nordicsemi.kotlin.mesh.core.SecurePropertiesStorage
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

private val Context.securePropertiesDataStore: DataStore<ProtoSecurePropertiesMap> by dataStore(
    fileName = "SecureProperties",
    serializer = SecurePropertiesSerializer
)


class MeshSecurePropertiesStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePropertiesStore: DataStore<ProtoSecurePropertiesMap>,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : SecurePropertiesStorage {

    override suspend fun ivIndex(uuid: UUID): IvIndex = securePropertiesStore.data.map {
        it.properties[uuid.toString()]?.ivIndex?.toIvIndex()
    }.first() ?: IvIndex().also { storeIvIndex(uuid, it) }

    override suspend fun storeIvIndex(uuid: UUID, ivIndex: IvIndex) {
        securePropertiesStore.updateData {
            it.properties[uuid.toString()]?.copy(ivIndex = ivIndex.toProtoIvIndex())
            it
        }
    }

    override suspend fun nextSequenceNumber(uuid: UUID, address: UnicastAddress): UInt {
        // Lets get the sequence number from the data store for a given address or 0 if it doesn't
        // exist
        val sequenceNumber = securePropertiesStore.data.map {
            it.properties[uuid.toString()]?.sequenceNumbers?.get(address.address.toInt())?.toUInt()
        }.first() ?: 0u
        storeNextSequenceNumber(
            uuid = uuid,
            address = address,
            sequenceNumber = (sequenceNumber + 1u)
        )
        return sequenceNumber
    }

    override suspend fun storeNextSequenceNumber(
        uuid: UUID,
        address: UnicastAddress,
        sequenceNumber: UInt
    ) {
        securePropertiesStore.updateData {
            val sequenceNumbers = it
                .properties[uuid.toString()]?.sequenceNumbers?.toMutableMap() ?: mutableMapOf()
            sequenceNumbers[address.address.toInt()] = sequenceNumber.toInt()
            it.properties[uuid.toString()]?.copy(sequenceNumbers = sequenceNumbers)
            it
        }
    }

    override suspend fun resetSequenceNumber(uuid: UUID, address: UnicastAddress) {
        storeNextSequenceNumber(uuid = uuid, address = address, sequenceNumber = 0u)
    }

    override suspend fun lastSeqAuthValue(uuid: UUID, source: UnicastAddress) =
        securePropertiesStore.data.map {
            it.properties[uuid.toString()]?.seqAuths?.get(source.address.toInt())?.last?.toULong()
        }.first() ?: 0uL

    override suspend fun storeLastSeqAuthValue(
        uuid: UUID,
        source: UnicastAddress,
        lastSeqAuth: ULong
    ) {
        securePropertiesStore.updateData {
            val seqAuths = it.properties[uuid.toString()]
                ?.seqAuths?.toMutableMap() ?: mutableMapOf()
            seqAuths[source.address.toInt()]?.copy(last = lastSeqAuth.toLong())
            it.properties[uuid.toString()]?.copy(seqAuths = seqAuths)
            it
        }
    }

    override suspend fun previousSeqAuthValue(uuid: UUID, source: UnicastAddress) =
        securePropertiesStore.data.map {
            it.properties[uuid.toString()]?.seqAuths?.get(source.address.toInt())?.previous?.toULong()
        }.first() ?: 0uL

    override suspend fun storePreviousSeqAuthValue(
        uuid: UUID,
        source: UnicastAddress,
        seqAuth: ULong
    ) {
        securePropertiesStore.updateData {
            val seqAuths = it.properties[uuid.toString()]
                ?.seqAuths?.toMutableMap() ?: mutableMapOf()
            seqAuths[source.address.toInt()]?.copy(previous = seqAuth.toLong())
            it.properties[uuid.toString()]?.copy(seqAuths = seqAuths)
            it
        }
    }
}

object SecurePropertiesSerializer : Serializer<ProtoSecurePropertiesMap> {
    override val defaultValue: ProtoSecurePropertiesMap
        get() = ProtoSecurePropertiesMap()

    override suspend fun readFrom(input: InputStream): ProtoSecurePropertiesMap {
        try {
            return ProtoSecurePropertiesMap.ADAPTER.decode(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: ProtoSecurePropertiesMap, output: OutputStream) {
        t.encode(output)
    }
}

/**
 * Converts a [ProtoIvIndex] to [IvIndex].
 */
private fun ProtoIvIndex.toIvIndex() = IvIndex(
    index = index.toUInt(),
    isIvUpdateActive = updateActive,
    transitionDate = Instant.fromEpochMilliseconds(transitionDate)
)

private fun IvIndex.toProtoIvIndex() = ProtoIvIndex(
    index = index.toInt(),
    updateActive = isIvUpdateActive,
    transitionDate = transitionDate.toEpochMilliseconds()
)