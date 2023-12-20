@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.kotlin.mesh.core.NetworkPropertiesStorage
import no.nordicsemi.kotlin.mesh.core.model.Address
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.toHex
import java.util.UUID
import javax.inject.Inject

class MeshNetworkPropertiesStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : NetworkPropertiesStorage {
    private var uuid: UUID? = null
    private lateinit var dataStore: DataStore<Preferences>

    override val sequenceNumbers: MutableMap<UnicastAddress, UInt> = mutableMapOf()

    override lateinit var ivIndex: IvIndex
    override lateinit var lastTransitionDate: Instant
    override var isIvRecoveryActive: Boolean = false

    override suspend fun load(scope: CoroutineScope, uuid: UUID, addresses: List<UnicastAddress>) {
        if (this.uuid != uuid) {
            this.uuid = uuid
            dataStore = PreferenceDataStoreFactory.create(
                corruptionHandler = ReplaceFileCorruptionHandler(
                    produceNewData = { emptyPreferences() }
                ),
                scope = CoroutineScope(ioDispatcher + SupervisorJob()),
                produceFile = { context.preferencesDataStoreFile(uuid.toString()) }
            )
        }
        dataStore.data.map {
            val index = it[PreferenceKeys.IV_INDEX]?.toUInt() ?: 0u
            val isIvUpdateActive = it[PreferenceKeys.IV_UPDATE_ACTIVE] ?: false
            ivIndex = IvIndex(index, isIvUpdateActive)
            val timestamp = it[PreferenceKeys.IV_TIMESTAMP] ?: 0L
            lastTransitionDate = Instant.fromEpochMilliseconds(timestamp)
            isIvRecoveryActive = it[PreferenceKeys.IV_RECOVERY] ?: false
            addresses.forEach { address ->
                sequenceNumbers[address] =
                    it[intPreferencesKey(address.address.toHex())]?.toUInt() ?: 0u
            }
        }.launchIn(scope)
    }

    override fun lastSeqAuthValue(source: Address) = dataStore.data.map { preferences ->
        preferences[longPreferencesKey(source.toHex(prefix0x = true))]?.toULong()
    }

    override suspend fun storeLastSeqAuthValue(lastSeqAuth: ULong, source: Address) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(source.toHex(prefix0x = true))] = lastSeqAuth.toLong()
        }
    }

    override fun previousSeqAuthValue(source: Address) = dataStore.data.map { preferences ->
        preferences[
            longPreferencesKey("P${source.toHex(prefix0x = true)}")
        ]?.toULong()
    }

    override suspend fun storePreviousSeqAuthValue(seqAuth: ULong, source: Address) {
        dataStore.edit { preferences ->
            preferences[
                longPreferencesKey("P${source.toHex(prefix0x = true)}")
            ] = seqAuth.toLong()
        }
    }

    override suspend fun removeSeqAuthValues(node: Node) {
        node.elements.forEach {
            removeSeqAuthValues(it.unicastAddress.address)
        }
    }

    override suspend fun removeSeqAuthValues(source: Address) {
        dataStore.edit { preferences ->
            preferences.remove(longPreferencesKey(source.toHex(prefix0x = true)))
            preferences.remove(longPreferencesKey("P${source.toHex(prefix0x = true)}"))
        }
    }

    override suspend fun save(uuid: UUID) {
        dataStore.edit { preferences ->
            preferences[PreferenceKeys.IV_INDEX] = ivIndex.index.toInt()
            preferences[PreferenceKeys.IV_UPDATE_ACTIVE] = ivIndex.isIvUpdateActive
            preferences[PreferenceKeys.IV_TIMESTAMP] = lastTransitionDate.toEpochMilliseconds()
            sequenceNumbers.forEach { (unicastAddress, sequence) ->
                preferences[intPreferencesKey(unicastAddress.address.toHex())] = sequence.toInt()
            }
        }
    }

    private object PreferenceKeys {
        //private const val SEQUENCE_NUMBER = "SEQUENCE_NUMBER"
        val IV_TIMESTAMP = longPreferencesKey("IV_TIMESTAMP")
        val IV_INDEX = intPreferencesKey("IV_INDEX")
        val IV_UPDATE_ACTIVE = booleanPreferencesKey("IV_UPDATE_ACTIVE")
        val IV_RECOVERY = booleanPreferencesKey("IV_RECOVERY")
    }
}
