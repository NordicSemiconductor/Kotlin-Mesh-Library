package no.nordicsemi.android.nrfmesh.core.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import no.nordicsemi.kotlin.mesh.core.Storage
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
) : Storage {

    // TODO these are not used anymore but are left for reference
    private val dataStream = dataStore.data.map { preferences ->
        val uuid = preferences[stringPreferencesKey(LAST_NETWORK)]
        if (uuid != null)
            preferences[stringPreferencesKey(uuid.toString())]
                .toString()
                .encodeToByteArray()
        else
            byteArrayOf()
    }


    override suspend fun load(): ByteArray? = dataStream.firstOrNull()

    // TODO consider looking in to storing library related information
    override suspend fun save(uuid: UUID, network: ByteArray) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(LAST_NETWORK)] = uuid.toString()
            preferences[stringPreferencesKey(uuid.toString())] = network.decodeToString()
        }
    }
}