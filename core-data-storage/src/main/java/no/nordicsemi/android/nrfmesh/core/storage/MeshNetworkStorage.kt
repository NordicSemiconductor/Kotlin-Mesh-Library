package no.nordicsemi.android.nrfmesh.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import no.nordicsemi.kotlin.mesh.core.LocalStorage
import java.util.*
import javax.inject.Inject

private const val FILE = "NETWORK_CONFIGURATION"
private const val LAST_NETWORK = "LAST_CONFIGURATION"

/**
 * Custom storage implementation using Jetpack DataStore.
 *
 * @param dataStore DataStore to be used to load or save the mesh network configuration locally.
 */
class MeshNetworkStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : LocalStorage {

    private val lastUsedNetworkUuid = dataStore.data.map {
        it[stringPreferencesKey(LAST_NETWORK)]
    }

    override suspend fun load(): Flow<ByteArray> {
        val uuid = lastUsedNetworkUuid.firstOrNull()
        if (uuid != null) {
            return dataStore.data.map {
                it[stringPreferencesKey(uuid.toString())].toString().encodeToByteArray()
            }
        }
        return flow { emit(byteArrayOf()) }
    }

    override suspend fun save(uuid: UUID, network: String) {
        // TODO consider looking in to storing library related information
        dataStore.edit {
            it[stringPreferencesKey(uuid.toString())] = network
            it[stringPreferencesKey(LAST_NETWORK)] = uuid.toString()
        }
    }
}