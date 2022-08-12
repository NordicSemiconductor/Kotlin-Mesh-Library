package no.nordicsemi.android.nrfmesh.feature.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.*
import java.io.BufferedReader
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())

    init {
        viewModelScope.launch {
            repository.network.collect {
                it.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                uiState = SettingsUiState(
                    networkName = it.name,
                    provisioners = it.provisioners,
                    networkKeys = it.networkKeys,
                    applicationKeys = it.applicationKeys,
                    scenes = it.scenes,
                    ivIndex = it.ivIndex,
                    lastModified = DateFormat.getDateTimeInstance().format(
                        Date(it.timestamp.toEpochMilliseconds())
                    )
                )
            }
        }
    }

    /**
     * Imports a network from a given Uri.
     *
     * @param uri                  URI of the file.
     * @param contentResolver      Content resolver.
     */
    internal fun importNetwork(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val networkJson = contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.reader()).use { bufferedReader ->
                    bufferedReader.readText()
                }
            } ?: ""
            repository.importMeshNetwork(networkJson.encodeToByteArray())
        }
    }
}

data class SettingsUiState(
    val networkName: String = "nRF Mesh",
    val provisioners: List<Provisioner> = emptyList(),
    val networkKeys: List<NetworkKey> = emptyList(),
    val applicationKeys: List<ApplicationKey> = emptyList(),
    val scenes: List<Scene> = emptyList(),
    val ivIndex: IvIndex = IvIndex(),
    val lastModified: String = DateFormat.getDateTimeInstance().format(
        Date(System.currentTimeMillis())
    )
)