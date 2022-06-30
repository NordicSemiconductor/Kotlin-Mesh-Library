package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import java.io.BufferedReader
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val storageRepository: DataStoreRepository
) : ViewModel() {

    fun importNetwork(uri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            val networkJson = contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(inputStream.reader()).use { bufferedReader ->
                    bufferedReader.readText()
                }
            } ?: ""
            storageRepository.importMeshNetwork(networkJson.encodeToByteArray())
        }
    }
}