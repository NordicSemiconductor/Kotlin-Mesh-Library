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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.core.RealServerDevice
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.data.DataStoreRepository
import no.nordicsemi.android.nrfmesh.destinations.netKeySelector
import no.nordicsemi.android.nrfmesh.destinations.provisioning
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
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

    private val bleScanResults = parameterOf(provisioning)
    private val pbGattBearer =
        PbGattBearer(context, bleScanResults.device as RealServerDevice)
    private var unprovisionedDevice: UnprovisionedDevice =
        UnprovisionedDevice.from(bleScanResults.lastScanResult!!.scanRecord!!.bytes)

    private var _uiState = MutableStateFlow(
        ProvisioningScreenUiState(
            unprovisionedDevice = unprovisionedDevice,
            provisionerState = ProvisionerState.Connecting
        )
    )
    internal val uiState = _uiState.asStateFlow()

    private lateinit var provisioningJob: Job

    init {
        observeNetwork()
        observeNetKeySelector()
        connect()
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            repository.network.collect {
                meshNetwork = it
            }
        }
    }

    private fun connect() {
        viewModelScope.launch() {
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
                if(state is ProvisioningState.Complete) {
                    // Save when the provisioning completes.
                    repository.save()
                }
            }.catch {

            }.launchIn(viewModelScope)
        }
    }

    internal fun disconnect() {
        provisioningJob.cancel()
    }

    private fun observeNetKeySelector() {
        resultFrom(netKeySelector)
            // Filter out results of cancelled navigation.
            .mapNotNull { it as? NavigationResult.Success }
            .map { it.value }
            // Save the result in SavedStateHandle.
            .onEach { keyIndex ->
                //savedStateHandle[KEY_INDEX] = it
                uiState.value.provisionerState.let { provisionerState ->
                    if (provisionerState is ProvisionerState.Provisioning) {
                        if (provisionerState.state is ProvisioningState.CapabilitiesReceived) {
                            meshNetwork.networkKeys.find { key ->
                                keyIndex == key.index.toInt()
                            }?.let {
                                provisionerState.state.configuration.networkKey = it
                            }
                        }
                    }
                }
            }
            // And finally, launch the flow in the ViewModelScope.
            .launchIn(viewModelScope)
    }

    internal fun onNameChanged(name: String) {
        unprovisionedDevice.name = name
        _uiState.value = _uiState.value.copy(unprovisionedDevice = unprovisionedDevice)
    }

    internal fun onAddressChanged(
        configuration: ProvisioningConfiguration,
        elementCount: Int,
        address: Int
    ) = runCatching {
        val unicastAddress = UnicastAddress(address.toUShort())
        provisioningManager.isUnicastAddressValid(
            unicastAddress = UnicastAddress(address.toUShort()),
            numberOfElements = elementCount
        ).also {
            configuration.unicastAddress = UnicastAddress(address.toUShort())
            val provisionerState =
                _uiState.value.provisionerState as ProvisionerState.Provisioning
            val state = provisionerState.state as ProvisioningState.CapabilitiesReceived
            state.configuration.unicastAddress = unicastAddress
            _uiState.value = _uiState.value.copy(provisionerState = provisionerState)
        }
    }

    /**
     * Checks if the given address is valid
     */
    internal fun isValidAddress(address: UShort): Boolean = when {
        UnicastAddress.isValid(address = address) -> true
        else -> throw Throwable("Invalid unicast address")
    }

    internal fun onNetworkKeyClick(keyIndex: KeyIndex) {
        navigateTo(netKeySelector, keyIndex.toInt())
    }

    internal fun onProvisionClick() {
        val state = uiState.value
        viewModelScope.launch {
            state.provisionerState.let {
                if (it is ProvisionerState.Provisioning) {
                    if (it.state is ProvisioningState.CapabilitiesReceived) {
                        it.state.run {
                            start(configuration)
                        }
                    }
                }
            }
        }
    }

    override fun log(message: String, category: LogCategory, level: LogLevel) {
        Log.println(level.toAndroidLogLevel(), category.category, message)
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