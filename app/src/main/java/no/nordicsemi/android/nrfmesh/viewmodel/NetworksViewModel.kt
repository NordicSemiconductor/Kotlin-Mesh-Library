package no.nordicsemi.android.nrfmesh.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NetworksViewModel @Inject constructor() : ViewModel() {

    var name by mutableStateOf("Home Office")
        private set

    fun onNameChanged(name: String) {
        this.name = name
    }
}