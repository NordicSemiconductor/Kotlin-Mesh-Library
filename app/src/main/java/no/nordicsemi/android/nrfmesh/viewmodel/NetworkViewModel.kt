package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.common.ui.scanner.model.DiscoveredBluetoothDevice
import no.nordicsemi.android.kotlin.ble.core.RealServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningManager
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import javax.inject.Inject

@HiltViewModel
class NetworkViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle) {

    var isNetworkLoaded by mutableStateOf(false)
        private set
    private lateinit var meshNetwork: MeshNetwork
    private lateinit var provisioningManager: ProvisioningManager

    init {
        loadNetwork()
    }

    /**
     * Loads the network
     */
    private fun loadNetwork() {
        viewModelScope.launch {
            isNetworkLoaded = repository.load()
            repository.network.collect {
                meshNetwork = it
            }
        }
    }

    fun connect(context: Context, device: DiscoveredBluetoothDevice) {
        viewModelScope.launch {
            device.scanResult?.scanRecord?.bytes?.let {
                val unprovisionedDevice = UnprovisionedDevice.from(it)
                val pbGattBearer = PbGattBearer(context, RealServerDevice(device.device))
                pbGattBearer.open()
                provisioningManager = ProvisioningManager(
                    unprovisionedDevice = unprovisionedDevice,
                    meshNetwork = meshNetwork,
                    bearer = pbGattBearer
                )
                provisioningManager.provision(10u).collect {

                }
            }
        }
    }
}