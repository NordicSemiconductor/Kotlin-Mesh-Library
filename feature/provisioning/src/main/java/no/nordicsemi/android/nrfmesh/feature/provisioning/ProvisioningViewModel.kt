package no.nordicsemi.android.nrfmesh.feature.provisioning

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.Utils.toAndroidLogLevel
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Connected
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Connecting
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Disconnected
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Error
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Provisioning
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Scanning
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.provisioning.ProvisioningBearer
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
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
    val savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository,
) : ViewModel(), Logger {

    private lateinit var meshNetwork: MeshNetwork
    private lateinit var provisioningManager: ProvisioningManager

    private var unprovisionedDevice: UnprovisionedDevice? = null
    private var keyIndex: KeyIndex = 0u
    private val _uiState = MutableStateFlow(
        ProvisioningScreenUiState(provisionerState = Scanning)
    )
    internal val uiState = _uiState.asStateFlow()

    init {
        observeNetwork()
        observeNetKeySelector()
    }

    /**
     * Observes the mesh network.
     */
    private fun observeNetwork() {
        repository.network
            .onEach {
                meshNetwork = it
                val state = _uiState.value
                _uiState.value = ProvisioningScreenUiState(
                    networkKeys = it.networkKeys.toList(),
                    nodes = it.nodes.toList(),
                    provisionerState = state.provisionerState
                )
            }
            .launchIn(scope = viewModelScope)
    }

    internal fun beginProvisioning(result: ScanResult) {
        viewModelScope.launch {
            val device = UnprovisionedDevice
                .from(advertisementData = result.advertisingData.raw)
                .also { this@ProvisioningViewModel.unprovisionedDevice = it }

            _uiState.value = _uiState.value.copy(
                provisionerState = Connecting(unprovisionedDevice = device)
            )
            val pbGattBearer = repository.connectOverPbGattBearer(device = result.peripheral)
            pbGattBearer.state
                .takeWhile { it !is BearerEvent.Closed }
                .onEach {
                    if (it is BearerEvent.Opened) {
                        _uiState.value = _uiState.value.copy(
                            provisionerState = Connected(unprovisionedDevice = device)
                        )
                        identifyNode(unprovisionedDevice = device, bearer = pbGattBearer)
                    }
                }.onCompletion {
                    println("What happened here: $it")
                    _uiState.value = _uiState.value.copy(
                        provisionerState = Disconnected(unprovisionedDevice = device)
                    )
                }.launchIn(scope = this)
        }
    }

    /**
     * Identify the node by sending a provisioning invite.
     *
     * @param unprovisionedDevice  Device to be provisioned.
     * @param bearer               Provisioning bearer to be used.
     */
    private fun identifyNode(unprovisionedDevice: UnprovisionedDevice, bearer: ProvisioningBearer) {
        provisioningManager = ProvisioningManager(
            unprovisionedDevice = unprovisionedDevice,
            meshNetwork = meshNetwork,
            bearer = bearer
        ).apply { logger = this@ProvisioningViewModel }

        provisioningManager.provision(attentionTimer = 10u)
            .onEach { state ->
                _uiState.value = _uiState.value.copy(
                    provisionerState = Provisioning(unprovisionedDevice, state)
                )
            }/*.catch { throwable ->
                log(
                    message = "Error while provisioning $throwable",
                    category = LogCategory.PROVISIONING,
                    level = LogLevel.ERROR
                )
                _uiState.value = _uiState.value.copy(
                    provisionerState = Error(unprovisionedDevice, throwable)
                )
            }*/.onCompletion { throwable ->
                _uiState.value.provisionerState.let { provisionerState ->
                    if (provisionerState is Provisioning) {
                        // Save when the provisioning completes.
                        if (throwable == null && provisionerState.state is ProvisioningState.Complete) {
                            repository.save()
                        }
                    }
                }
            }.launchIn(scope = viewModelScope)
    }

    /**
     * Disconnect from the device.
     */
    internal fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
            _uiState.value = ProvisioningScreenUiState(
                provisionerState = Scanning
            )
        }
    }

    /**
     * Observers the result of the NetKeySelector destination.
     */
    private fun observeNetKeySelector() {
        savedStateHandle.getStateFlow(
            key = MeshNavigationDestination.ARG,
            initialValue = "0"
        ).onEach { index ->
            val state = _uiState.value
            keyIndex = index.toInt().toUShort()
            state.provisionerState.let {
                if (it is Provisioning) {
                    if (it.state is ProvisioningState.CapabilitiesReceived) {
                        it.state.parameters.networkKey = meshNetwork.networkKeys.find { key ->
                            keyIndex == key.index
                        } ?: throw Throwable("Network key not found")
                        _uiState.value = state.copy(provisionerState = it)
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Invoked when the user changes the name of the device.
     *
     * @param name New name to be assigned to the device.
     */
    internal fun onNameChanged(name: String) {
        unprovisionedDevice?.name = name
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
        address: Int,
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

    internal fun onNetworkKeyClicked(key: NetworkKey) {
        val provisionerState = _uiState.value.provisionerState
        if (provisionerState is Provisioning &&
            provisionerState.state is ProvisioningState.CapabilitiesReceived
        ) {
            println("We are here")
            val provisioningState = provisionerState.state
            val p = provisioningState.parameters.apply {
                networkKey = key
            }
            //provisioningState = provisioningState.copy(parameters = parameters)
            _uiState.value = _uiState.value.copy(
                provisionerState = provisionerState.copy(
                    state = provisioningState.copy(
                        parameters = p
                    )
                )
            )
            println("We are here: ${_uiState.value.provisionerState}")
        }
    }

    /**
     * Starts the provisioning process after the identification is completed.
     *
     * @param authMethod Authentication method to be used.
     */
    internal fun onAuthenticationMethodSelected(authMethod: AuthenticationMethod) {
        val state = uiState.value
        viewModelScope.launch {
            state.provisionerState.let {
                if (it is Provisioning) {
                    if (it.state is ProvisioningState.CapabilitiesReceived) {
                        it.state.run {
                            parameters.authMethod = authMethod
                            parameters.networkKey = meshNetwork.networkKeys.find { key ->
                                0.toUShort() == key.index
                            } ?: throw Throwable("Network key not found")
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
            is AuthAction.ProvideStaticKey -> method.authenticate(input.toByteArray())
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
    }

    /**
     * Invoked when the provisioning process completes and navigates to the list of nodes.
     */
    internal fun onProvisioningFailed() {
        disconnect()
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
        val state: ProvisioningState,
    ) : ProvisionerState()

    data class Error(val unprovisionedDevice: UnprovisionedDevice, val throwable: Throwable) :
        ProvisionerState()

    data class Disconnected(val unprovisionedDevice: UnprovisionedDevice) : ProvisionerState()
}

internal data class ProvisioningScreenUiState(
    val networkKeys: List<NetworkKey> = emptyList(),
    val nodes: List<Node> = emptyList(),
    val provisionerState: ProvisionerState
)