@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.map
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.Dispatcher
import no.nordicsemi.android.nrfmesh.core.common.dispatchers.MeshDispatchers
import no.nordicsemi.kotlin.mesh.core.NetworkPropertiesStorage
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.util.UUID
import javax.inject.Inject

class MeshNetworkPropertiesStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(MeshDispatchers.IO) private val ioDispatcher: CoroutineDispatcher
) : NetworkPropertiesStorage {
    private var uuid: UUID? = null
    private var dataStore: DataStore<Preferences>? = null

    override val sequenceNumbers: MutableMap<UnicastAddress, UInt> = mutableMapOf()

    override lateinit var ivIndex: IvIndex

    override suspend fun load(uuid: UUID) {
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
        dataStore?.data?.map {
            val index = it[stringPreferencesKey(IV_INDEX)]?.toUInt() ?: 0u
            val isIvUpdateActive = it[stringPreferencesKey(IV_UPDATE_ACTIVE)]?.toBoolean() ?: false
            ivIndex = IvIndex(index, isIvUpdateActive)
            // TODO clarify how to load sequence numbers.
        }

    }

    override suspend fun save(uuid: UUID) {
        dataStore?.edit { preferences ->
            preferences[stringPreferencesKey(IV_INDEX)] = ivIndex.index.toString()
            preferences[stringPreferencesKey(IV_UPDATE_ACTIVE)] =
                ivIndex.isIvUpdateActive.toString()
            // TODO clarify how to save sequence numbers.
            /*val seq = sequenceNumbers.entries.joinToString {
                "${it.key.address.toHex()}=${it.value}"
            }*/

        }
    }

    companion object {
        private const val SEQUENCE_NUMBER = "SEQUENCE_NUMBER"
        private const val IV_TIMESTAMP = "IV_TIMESTAMP"
        private const val IV_INDEX = "IV_INDEX"
        private const val IV_UPDATE_ACTIVE = "IV_UPDATE_ACTIVE"
    }
}