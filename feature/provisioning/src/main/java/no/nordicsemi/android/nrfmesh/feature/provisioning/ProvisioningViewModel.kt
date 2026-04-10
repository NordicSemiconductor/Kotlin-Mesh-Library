package no.nordicsemi.android.nrfmesh.feature.provisioning

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Timer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.di.IoDispatcher
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.core.data.DeveloperSettings
import no.nordicsemi.android.nrfmesh.core.data.configurator.ConfigTask
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Connected
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Connecting
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Disconnected
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Error
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Provisioning
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Scanning
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.bearer.BearerEvent
import no.nordicsemi.kotlin.mesh.bearer.provisioning.ProvisioningBearer
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigCompositionDataGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigDefaultTtlGet
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.logger.LogCategory
import no.nordicsemi.kotlin.mesh.logger.LogLevel
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningManager
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningParameters
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@HiltViewModel
class ProvisioningViewModel @Inject constructor(
    private val repository: CoreDataRepository,
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher,
) : ViewModel() {
    private var meshNetwork: MeshNetwork? = null
    private var provisioningManager: ProvisioningManager? = null
    private var unprovisionedDevice: UnprovisionedDevice? = null
    private var selectedScanResult: ScanResult? = null
    private var selectedNode: Node? = null


    private val _uiState = MutableStateFlow(
        value = ProvisioningScreenUiState(provisionerState = Scanning)
    )
    internal val uiState = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = ProvisioningScreenUiState(provisionerState = Scanning)
        )

    init {
        observeNetwork()
        observeDeveloperSettings()
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
                    provisionerState = state.provisionerState,
                    developerSettings = state.developerSettings
                )
            }
            .launchIn(scope = viewModelScope)
    }

    private fun observeDeveloperSettings() {
        repository.developerSettingsStateFlow.onEach {
            _uiState.update { state ->
                state.copy(developerSettings = it)
            }
        }.launchIn(scope = viewModelScope)
    }

    /**
     * Checks if the given device is already provisioned.
     *
     * @param scanResult Scan result of the device to be checked.
     * @return True if the device is already provisioned, false otherwise.
     */
    @OptIn(ExperimentalUuidApi::class)
    internal fun isDeviceAlreadyProvisioned(scanResult: ScanResult) = try {
        val device = UnprovisionedDevice.from(advertisementData = scanResult.advertisingData.raw)
        selectedScanResult = scanResult
        meshNetwork?.node(uuid = device.uuid)?.also {
            selectedNode = it
        } != null
    } catch (_: Exception) {
        false
    }

    /**
     * Starts the provisioning process by identifying the node
     */
    internal fun beginProvisioning() {
        val scanResult = selectedScanResult ?: return
        viewModelScope.launch {
            val device = UnprovisionedDevice
                .from(advertisementData = scanResult.advertisingData.raw)
                .also { this@ProvisioningViewModel.unprovisionedDevice = it }
            _uiState.update {
                it.copy(provisionerState = Connecting(unprovisionedDevice = device))
            }
            val pbGattBearer = repository.connectOverPbGattBearer(device = scanResult.peripheral)
            pbGattBearer.state
                .takeWhile { it !is BearerEvent.Closed }
                .onEach {
                    if (it is BearerEvent.Opened) {
                        _uiState.update {
                            it.copy(provisionerState = Connected(unprovisionedDevice = device))
                        }
                        identifyNode(unprovisionedDevice = device, bearer = pbGattBearer)
                    }
                }.onCompletion {
                    _uiState.update {
                        it.copy(provisionerState = Disconnected(unprovisionedDevice = device))
                    }
                }.launchIn(scope = this)
        }
    }

    /**
     * Identify the node by sending a provisioning invite.
     *
     * @param unprovisionedDevice  Device to be provisioned.
     * @param bearer               Provisioning bearer to be used.
     */
    private fun identifyNode(
        unprovisionedDevice: UnprovisionedDevice,
        bearer: ProvisioningBearer,
    ) {
        meshNetwork?.let {
            val provisioningManager = ProvisioningManager(
                unprovisionedDevice = unprovisionedDevice,
                meshNetwork = it,
                bearer = bearer,
                ioDispatcher = dispatcher
            ).apply {
                logger = repository
                this@ProvisioningViewModel.provisioningManager = this
            }

            provisioningManager.provision(attentionTimer = 10u)
                .onEach { state ->
                    _uiState.update { uiState ->
                        uiState.copy(
                            provisionerState = Provisioning(
                                unprovisionedDevice = unprovisionedDevice,
                                state = state
                            ),
                        )
                    }
                    state.run {
                        if (this is ProvisioningState.CapabilitiesReceived) {
                            if (capabilities.supportedAuthMethods.contains(AuthenticationMethod.NoOob) &&
                                _uiState.value.developerSettings.quickProvisioning
                            ) {
                                // Added a delay to allow the UI to update during quick provisioning
                                // If not everything happens too fast.
                                parameters.authMethod = AuthenticationMethod.NoOob
                                start(parameters)
                            }
                        }
                    }
                }.catch { throwable ->
                    repository.log(
                        message = { "Error while provisioning $throwable" },
                        category = LogCategory.PROVISIONING,
                        level = LogLevel.ERROR
                    )
                    _uiState.value = _uiState.value.copy(
                        provisionerState = Error(
                            unprovisionedDevice = unprovisionedDevice,
                            throwable = throwable
                        )
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
                }.launchIn(scope = viewModelScope)
        }
    }

    /**
     * Disconnect from the device.
     */
    internal fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
            _uiState.update { it.copy(provisionerState = Scanning) }
        }
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
        provisioningManager?.isUnicastAddressValid(
            unicastAddress = UnicastAddress(address.toUShort()),
            numberOfElements = elementCount
        ).also {
            configuration.unicastAddress = UnicastAddress(address.toUShort())
            val provisionerState =
                _uiState.value.provisionerState as Provisioning
            val state = provisionerState.state as ProvisioningState.CapabilitiesReceived
            state.parameters.unicastAddress = unicastAddress
            _uiState.update {
                it.copy(provisionerState = provisionerState)
            }
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
            val provisioningState = provisionerState.state
            val provisioningParameters = provisioningState
                .parameters
                .apply { networkKey = key }
            _uiState.update {
                it.copy(
                    provisionerState = provisionerState.copy(
                        state = provisioningState.copy(
                            parameters = provisioningParameters
                        )
                    )
                )
            }
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
    @OptIn(ExperimentalUuidApi::class)
    internal fun onProvisioningComplete(uuid: Uuid) {
        // Queue ConfigCompositionDataGet and ConfigDefaultTtl
        // When provisioning is complete we enqueue the next configuration tasks that can be resumed
        val messenger = repository.messengers.createMessenger(nodeUuid = uuid)
        messenger.enqueueTask(
            task = ConfigTask(
                icon = Icons.Outlined.DeviceHub,
                label = "Reading composition data",
                message = ConfigCompositionDataGet(page = 0x00u)
            )
        )
        if (_uiState.value.developerSettings.alwaysReconfigure) {
            selectedNode?.let {
                messenger.enqueueReconfigurationWith(originalNode = it)
            }
        } else {
            messenger.enqueueTask(
                task = ConfigTask(
                    icon = Icons.Outlined.Timer,
                    label = "Reading default TTL",
                    message = ConfigDefaultTtlGet()
                )
            )
        }
        viewModelScope.launch {
            repository.disconnect()
            _uiState.update { it.copy(provisionerState = Scanning) }
            repository.startAutomaticConnectivity(meshNetwork = meshNetwork)
        }
    }

    /**
     * Invoked when the provisioning process completes and navigates to the list of nodes.
     */
    internal fun onProvisioningFailed() {
        disconnect()
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

    data class Error(
        val unprovisionedDevice: UnprovisionedDevice,
        val throwable: Throwable,
    ) : ProvisionerState()

    data class Disconnected(val unprovisionedDevice: UnprovisionedDevice) : ProvisionerState()
}

internal data class ProvisioningScreenUiState(
    val networkKeys: List<NetworkKey> = emptyList(),
    val nodes: List<Node> = emptyList(),
    val provisionerState: ProvisionerState,
    val developerSettings: DeveloperSettings = DeveloperSettings(),
)