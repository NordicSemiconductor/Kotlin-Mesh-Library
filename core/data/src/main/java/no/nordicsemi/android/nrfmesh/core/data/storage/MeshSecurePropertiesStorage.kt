@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.data.storage

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.common.di.DefaultDispatcher
import no.nordicsemi.android.nrfmesh.core.common.di.IoDispatcher
import no.nordicsemi.android.nrfmesh.core.data.ProtoIvIndex
import no.nordicsemi.android.nrfmesh.core.data.ProtoSecureProperties
import no.nordicsemi.android.nrfmesh.core.data.ProtoSecurePropertiesMap
import no.nordicsemi.android.nrfmesh.core.data.ProtoSeqAuth
import no.nordicsemi.kotlin.mesh.core.SecurePropertiesStorage
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import javax.inject.Inject

class MeshSecurePropertiesStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePropertiesStore: DataStore<ProtoSecurePropertiesMap>,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SecurePropertiesStorage {

    private val scope = CoroutineScope(ioDispatcher + SupervisorJob())

    /**
     * Creates a default [ProtoSecurePropertiesMap] with a single entry for the given [uuid].
     *
     * @param uuid UUID of the mesh network.
     * @return [ProtoSecurePropertiesMap] with a single entry for the given [uuid].
     */
    private fun createProtoSecurePropertiesMap(uuid: UUID) = ProtoSecurePropertiesMap(
        properties = mutableMapOf(
            uuid.toString() to ProtoSecureProperties(
                ivIndex = ProtoIvIndex(),
                sequenceNumbers = mutableMapOf(),
                seqAuths = mutableMapOf()
            )
        )
    ).also {
        storeSecurePropertiesMap(it)
    }

    /**
     * Returns the [ProtoSecurePropertiesMap] for the given [uuid] or creates a new one for a given
     * [uuid] if it doesn't exist.
     *
     * @param uuid UUID of the mesh network.
     * @return [ProtoSecurePropertiesMap] for the given [uuid].
     */
    private suspend fun secureProperties(uuid: UUID) = securePropertiesStore.data
        .firstOrNull()
        ?.properties
        ?.get(uuid.toString())
        ?: createProtoSecurePropertiesMap(uuid).properties[uuid.toString()]

    private fun storeSecurePropertiesMap(securePropertiesMap: ProtoSecurePropertiesMap) {
        scope.launch {
            securePropertiesStore.updateData {
                it.copy(properties = securePropertiesMap.properties)
                it
            }
        }
    }

    override suspend fun ivIndex(uuid: UUID): IvIndex = secureProperties(uuid = uuid)
        ?.ivIndex?.toIvIndex() ?: IvIndex().also {
        storeIvIndex(uuid, it)
    }

    override suspend fun storeIvIndex(uuid: UUID, ivIndex: IvIndex) {
        scope.launch {
            securePropertiesStore.updateData {
                it.properties[uuid.toString()]?.copy(ivIndex = ivIndex.toProtoIvIndex())
                it
            }
        }
    }

    override suspend fun nextSequenceNumber(uuid: UUID, address: UnicastAddress): UInt {
        // Lets get the sequence number from the data store for a given address or 0 if it doesn't
        // exist
        val sequenceNumber = secureProperties(uuid = uuid)
            ?.sequenceNumbers
            ?.get(key = address.address.toInt())?.toUInt() ?: 0u
        // Increment the sequence number and store it back to the data store
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
        sequenceNumber: UInt,
    ) {
        scope.launch {
            securePropertiesStore.updateData { securePropertiesMap ->
                val propertiesMap = securePropertiesMap
                    .properties
                    .toMutableMap()
                var properties = propertiesMap[uuid.toString()]
                    ?: ProtoSecureProperties()
                val sequenceNumbers = properties
                    .sequenceNumbers
                    .toMutableMap()
                    .also { map ->
                        map[address.address.toInt()] = sequenceNumber.toInt()
                    }
                properties = properties.copy(sequenceNumbers = sequenceNumbers)
                propertiesMap[uuid.toString()] = properties
                securePropertiesMap.copy(properties = propertiesMap.toMap())
            }
        }
    }

    override suspend fun resetSequenceNumber(uuid: UUID, address: UnicastAddress) {
        storeNextSequenceNumber(uuid = uuid, address = address, sequenceNumber = 0u)
    }

    override suspend fun lastSeqAuthValue(uuid: UUID, source: UnicastAddress) =
        secureProperties(uuid = uuid)
            ?.seqAuths
            ?.get(source.address.toInt())
            ?.last?.toULong() //?: 0uL

    override suspend fun storeLastSeqAuthValue(
        uuid: UUID,
        source: UnicastAddress,
        lastSeqAuth: ULong,
    ) {
        scope.launch {
            securePropertiesStore.updateData { securePropertiesMap ->
                val propertiesMap = securePropertiesMap
                    .properties
                    .toMutableMap()
                var properties = propertiesMap[uuid.toString()]
                    ?: ProtoSecureProperties()

                val seqAuths = properties
                    .seqAuths
                    .toMutableMap()
                var protoSeqAuth = seqAuths[source.address.toInt()] ?: ProtoSeqAuth()
                protoSeqAuth = protoSeqAuth.copy(last = lastSeqAuth.toLong())
                seqAuths[source.address.toInt()] = protoSeqAuth
                properties = properties.copy(seqAuths = seqAuths)
                propertiesMap[uuid.toString()] = properties
                securePropertiesMap.copy(properties = propertiesMap.toMap())
            }
        }
    }

    override suspend fun previousSeqAuthValue(uuid: UUID, source: UnicastAddress) =
        secureProperties(uuid = uuid)
            ?.seqAuths?.get(key = source.address.toInt())
            ?.previous?.toULong() ?: 0uL

    override suspend fun storePreviousSeqAuthValue(
        uuid: UUID,
        source: UnicastAddress,
        seqAuth: ULong,
    ) {
        scope.launch {
            securePropertiesStore.updateData { securePropertiesMap ->
                val propertiesMap = securePropertiesMap
                    .properties
                    .toMutableMap()
                var properties = propertiesMap[uuid.toString()]
                    ?: ProtoSecureProperties()

                val seqAuths = properties
                    .seqAuths
                    .toMutableMap()
                var protoSeqAuth = seqAuths[source.address.toInt()] ?: ProtoSeqAuth()
                protoSeqAuth = protoSeqAuth.copy(previous = seqAuth.toLong())
                seqAuths[source.address.toInt()] = protoSeqAuth
                properties = properties.copy(seqAuths = seqAuths)
                propertiesMap[uuid.toString()] = properties
                securePropertiesMap.copy(properties = propertiesMap.toMap())
            }
        }
    }
}

/**
 * Serializer for [ProtoSecurePropertiesMap] to be used when writing and reading from Proto
 * DataStore.
 */
object ProtoSecurePropertiesMapSerializer : Serializer<ProtoSecurePropertiesMap> {
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
 * @receiver [ProtoIvIndex]
 * @return [IvIndex]
 */
private fun ProtoIvIndex.toIvIndex() = IvIndex(
    index = index.toUInt(),
    isIvUpdateActive = updateActive,
    transitionDate = Instant.fromEpochMilliseconds(transitionDate)
)

/**
 * Converts a [IvIndex] to [ProtoIvIndex].
 *
 * @receiver [IvIndex]
 * @return [ProtoIvIndex]
 */
private fun IvIndex.toProtoIvIndex() = ProtoIvIndex(
    index = index.toInt(),
    updateActive = isIvUpdateActive,
    transitionDate = transitionDate.toEpochMilliseconds()
)