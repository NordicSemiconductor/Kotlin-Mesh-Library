package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.core.RealServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.destinations.provisioning
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.LogLevel
import no.nordicsemi.kotlin.mesh.logger.Logger
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningConfiguration
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningManager
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import javax.inject.Inject

@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    @ApplicationContext context: Context,
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: DataStoreRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle), Logger {

    private lateinit var meshNetwork: MeshNetwork
    private lateinit var provisioningManager: ProvisioningManager

    private val discoveredBluetoothDevice = parameterOf(provisioning)
    private val pbGattBearer =
        PbGattBearer(context, RealServerDevice(discoveredBluetoothDevice.device))
    private var unprovisionedDevice: UnprovisionedDevice =
        UnprovisionedDevice.from(discoveredBluetoothDevice.scanResult!!.scanRecord!!.bytes!!)

    private var _uiState = MutableStateFlow(
        ProvisioningScreenUiState(
            unprovisionedDevice = unprovisionedDevice,
            provisionerState = ProvisionerState.Connecting
        )
    )
    internal val uiState = _uiState.asStateFlow()

    private lateinit var provisioningJob: Job

    init {
        viewModelScope.launch {
            repository.network.collect {
                meshNetwork = it
            }
        }
        connect()
    }

    private fun connect() {
        viewModelScope.launch {
            pbGattBearer.open()
            _uiState.value = ProvisioningScreenUiState(
                unprovisionedDevice = unprovisionedDevice,
                provisionerState = ProvisionerState.Connected
            )
            provisioningManager = ProvisioningManager(
                unprovisionedDevice = unprovisionedDevice,
                meshNetwork = meshNetwork,
                bearer = pbGattBearer
            ).apply {
                logger = this@ProvisioningViewModel
            }
            provisioningJob = provisioningManager.provision(10u).onEach { state ->
                _uiState.value = ProvisioningScreenUiState(
                    unprovisionedDevice = unprovisionedDevice,
                    provisionerState = ProvisionerState.Provisioning(state)
                )
            }.onCompletion {
                pbGattBearer.close()
                _uiState.value = ProvisioningScreenUiState(
                    unprovisionedDevice = unprovisionedDevice,
                    provisionerState = ProvisionerState.Disconnected
                )
            }.launchIn(viewModelScope)
        }
    }

    internal fun disconnect() {
        provisioningJob.cancel()
    }

    fun onNameChanged(name: String) {
        unprovisionedDevice.name = name
        _uiState.value = _uiState.value.copy(unprovisionedDevice = unprovisionedDevice)
    }

    override fun log(message: String, category: LogCategory, level: LogLevel) {
        Log.println(level.toAndroidLogLevel(), category.category, message)
    }

    fun onAddressChanged(
        configuration: ProvisioningConfiguration,
        elementCount: Int,
        address: Int
    ) = runCatching {
        // TODO check if address is valid
    }.onSuccess { }

    /**
     * Checks if the given address is valid
     */
    fun isValidAddress(address: UShort): Boolean = when {
        UnicastAddress.isValid(address = address) -> true
        else -> throw Throwable("Invalid unicast address")
    }
}

sealed class ProvisionerState {
    object Connecting : ProvisionerState()
    object Connected : ProvisionerState()
    object Identifying : ProvisionerState()
    data class Provisioning(
        val state: ProvisioningState
    ) : ProvisionerState()

    object Disconnected : ProvisionerState()
}

internal data class ProvisioningScreenUiState internal constructor(
    val unprovisionedDevice: UnprovisionedDevice,
    val provisionerState: ProvisionerState
)