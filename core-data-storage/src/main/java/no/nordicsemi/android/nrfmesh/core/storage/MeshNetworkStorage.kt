package no.nordicsemi.android.nrfmesh.core.storage

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
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

    override val dataStream = dataStore.data.map { preferences ->
        Log.d("AAAA", "Update triggered?")
        val uuid = preferences[stringPreferencesKey(LAST_NETWORK)]
        if (uuid != null)
            preferences[stringPreferencesKey(uuid.toString())]
                .toString()
                .encodeToByteArray()
        else
            byteArrayOf()
    }

    // TODO consider looking in to storing library related information
    override suspend fun save(uuid: UUID, network: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(LAST_NETWORK)] = uuid.toString()
            preferences[stringPreferencesKey(uuid.toString())] = network
        }
    }
}