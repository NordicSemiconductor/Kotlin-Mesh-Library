package no.nordicsemi.android.nrfmesh.feature.export

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val repository: DataStoreRepository
) : ViewModel() {

    var exportUiState by mutableStateOf(ExportUiState())
        private set

    fun onExportEverythingChecked(flag: Boolean) {
        exportUiState = exportUiState.copy(isExportEverythingChecked = flag)
    }

}

data class ExportUiState(val isExportEverythingChecked: Boolean = true)