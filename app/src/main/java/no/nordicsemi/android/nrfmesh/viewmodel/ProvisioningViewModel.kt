package no.nordicsemi.android.nrfmesh.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.NavigationResult
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.viewmodel.SimpleNavigationViewModel
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.mesh.bearer.pbgatt.PbGattBearer
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.destinations.netKeySelector
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.*
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.crypto.Utils.decodeHex
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.LogLevel
import no.nordicsemi.kotlin.mesh.logger.Logger
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningManager
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningParameters
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import javax.inject.Inject

@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    navigator: Navigator,
    private val repository: CoreDataRepository
) : SimpleNavigationViewModel(navigator = navigator, savedStateHandle = savedStateHandle), Logger {

    private lateinit var meshNetwork: MeshNetwork
    private lateinit var provisioningManager: ProvisioningManager

    private var unprovisionedDevice: UnprovisionedDevice? = null

    private val _uiState = MutableStateFlow(ProvisioningScreenUiState(provisionerState = Scanning))
    internal val uiState = _uiState.asStateFlow()

    init {
        observeNetwork()
        observeNetKeySelector()
    }

    /**
     * Observes the mesh network.
     */
    private fun observeNetwork() {
        repository.network.onEach {
            meshNetwork = it
        }.launchIn(viewModelScope)
    }

    /**
     * Connect to the device and begin provisioning.
     *
     * @param context     Context.
     * @param scanResults Scan results.
     */
    internal fun beginProvisioning(context: Context, scanResults: BleScanResults) {
        viewModelScope.launch {
            val device = UnprovisionedDevice.from(
                advertisementData = scanResults.lastScanResult!!.scanRecord!!.bytes!!.value
            ).also {
                this@ProvisioningViewModel.unprovisionedDevice = it
            }
            _uiState.value = ProvisioningScreenUiState(
                provisionerState = Connecting(unprovisionedDevice = device)
            )
            val pbGattBearer = repository.connectOverPbGattBearer(
                context = context,
                device = scanResults.device
            )
            pbGattBearer.state.takeWhile {
                it !is BearerEvent.Closed
            }.onEach {
                if (it is BearerEvent.Opened) {
                    _uiState.value = ProvisioningScreenUiState(
                        provisionerState = Connected(device)
                    )
                    identifyNode(device, pbGattBearer)
                }
            }.onCompletion {
                _uiState.value = ProvisioningScreenUiState(
                    provisionerState = Disconnected(device)
                )

            }.launchIn(this)
        }
    }

    /**
     * Identify the node by sending a provisioning invite.
     */
    private fun identifyNode(unprovisionedDevice: UnprovisionedDevice, pbGattBearer: PbGattBearer) {
        provisioningManager = ProvisioningManager(
            unprovisionedDevice = unprovisionedDevice,
            meshNetwork = meshNetwork,
            bearer = pbGattBearer
        ).apply { logger = this@ProvisioningViewModel }

        provisioningManager.provision(10u).onEach { state ->
            _uiState.value = ProvisioningScreenUiState(
                provisionerState = Provisioning(unprovisionedDevice, state)
            )
        }.catch { throwable ->
            log(
                message = "Error while provisioning $throwable",
                category = LogCategory.PROVISIONING,
                level = LogLevel.ERROR
            )
            _uiState.value = ProvisioningScreenUiState(
                provisionerState = Error(unprovisionedDevice, throwable)
            )
        }.onCompletion { throwable ->
            _uiState.value.provisionerState.let { provisionerState ->
                if (provisionerState is Provisioning) {
                    // Save when the provisioning completes.
                    if (throwable == null && provisionerState.state is ProvisioningState.Complete) {
                        repository.save()
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Disconnect from the device.
     */
    internal fun disconnect() {
        viewModelScope.launch {
            repository.close()
        }
    }

    /**
     * Observers the result of the NetKeySelector destination.
     */
    private fun observeNetKeySelector() {
        resultFrom(netKeySelector)
            // Filter out results of cancelled navigation.
            .mapNotNull { it as? NavigationResult.Success }
            .map { it.value }
            // Save the result in SavedStateHandle.
            .onEach { keyIndex ->
                //savedStateHandle[KEY_INDEX] = it
                uiState.value.provisionerState.let { provisionerState ->
                    if (provisionerState is Provisioning) {
                        if (provisionerState.state is ProvisioningState.CapabilitiesReceived) {
                            meshNetwork.networkKeys.find { key ->
                                keyIndex == key.index.toInt()
                            }?.let {
                                provisionerState.state.parameters.networkKey = it
                            }
                        }
                    }
                }
            }
            // And finally, launch the flow in the ViewModelScope.
            .launchIn(viewModelScope)
    }

    /**
     * Invoked when the user changes the name of the device.
     *
     * @param name New name to be assigned to the device.
     */
    internal fun onNameChanged(name: String) {
        unprovisionedDevice?.name = name
        //_uiState.value = _uiState.value.copy(unprovisionedDevice = unprovisionedDevice)
    }

    /**
     * Invoked when the user changes the unicast address.
     *
     * @param configuration Provisioning configuration containing the unicast address.
     * @param elementCount  Number of elements.
     * @param address       Address to be assigned to the node.
     */
    internal fun onAddressChanged(
        configuration: ProvisioningParameters,
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
                _uiState.value.provisionerState as Provisioning
            val state = provisionerState.state as ProvisioningState.CapabilitiesReceived
            state.parameters.unicastAddress = unicastAddress
            _uiState.value = _uiState.value.copy(provisionerState = provisionerState)
        }
    }

    /**
     * Checks if the given address is valid.
     */
    internal fun isValidAddress(address: UShort): Boolean = when {
        UnicastAddress.isValid(address = address) -> true
        else -> throw Throwable("Invalid unicast address")
    }

    /**
     * Navigates to the network key selector.
     *
     * @param keyIndex Index of the network key.
     */
    internal fun onNetworkKeyClick(keyIndex: KeyIndex) {
        navigateTo(netKeySelector, keyIndex.toInt())
    }

    /**
     * Starts the provisioning process after the identification is completed.
     *
     * @param authMethod Authentication method to be used.
     */
    internal fun startProvisioning(authMethod: AuthenticationMethod) {
        val state = uiState.value
        viewModelScope.launch {
            state.provisionerState.let {
                if (it is Provisioning) {
                    if (it.state is ProvisioningState.CapabilitiesReceived) {
                        it.state.run {
                            parameters.authMethod = authMethod
                            start(parameters)
                        }
                    }
                }
            }
        }
    }

    /**
     * Invoked when the user selects an authentication method.
     *
     * @param method Authentication method to be used.
     * @param input  Authentication input.
     */
    fun authenticate(method: AuthAction, input: String) {
        when (method) {
            is AuthAction.ProvideAlphaNumeric -> method.authenticate(input)
            is AuthAction.ProvideNumeric -> method.authenticate(input.toUInt())
            is AuthAction.ProvideStaticKey -> method.authenticate(input.decodeHex())
            is AuthAction.DisplayAlphaNumeric, is AuthAction.DisplayNumber -> {
                // Do nothing
            }
        }
    }

    /**
     * Invoked when the provisioning process completes and navigates to the list of nodes.
     */
    internal fun onProvisioningComplete() {
        disconnect()
        navigateUp() // Navigates back to the list of nodes
    }

    /**
     * Invoked when the provisioning process completes and navigates to the list of nodes.
     */
    internal fun onProvisioningFailed() {
        disconnect()
        navigateUp() // Navigates back to the scanner screen
    }

    override fun log(message: String, category: LogCategory, level: LogLevel) {
        Log.println(level.toAndroidLogLevel(), category.category, message)
    }
}

/**
 * ProvisionerState represents the state of the provisioning process for the UI.
 */
sealed class ProvisionerState {
    data object Scanning : ProvisionerState()
    data class Connecting(val unprovisionedDevice: UnprovisionedDevice) : ProvisionerState()
    data class Connected(val unprovisionedDevice: UnprovisionedDevice) : ProvisionerState()
    data class Identifying(val unprovisionedDevice: UnprovisionedDevice) : ProvisionerState()
    data class Provisioning(
        val unprovisionedDevice: UnprovisionedDevice,
        val state: ProvisioningState
    ) : ProvisionerState()

    data class Error(val unprovisionedDevice: UnprovisionedDevice, val throwable: Throwable) :
            ProvisionerState()

    data class Disconnected(val unprovisionedDevice: UnprovisionedDevice) : ProvisionerState()
}

internal data class ProvisioningScreenUiState internal constructor(
    val provisionerState: ProvisionerState
)