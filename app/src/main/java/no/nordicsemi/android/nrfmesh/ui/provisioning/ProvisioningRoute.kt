@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.ui.provisioning

import android.content.Context
import android.os.ParcelUuid
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.nordicLightGray
import no.nordicsemi.android.common.theme.nordicRed
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerView
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.Connected
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.Connecting
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.Disconnected
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.Error
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.Identifying
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState.Provisioning
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisioningScreenUiState
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisioningViewModel
import no.nordicsemi.kotlin.mesh.core.exception.NodeAlreadyExists
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningParameters
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice

@Composable
fun ProvisioningRoute(viewModel: ProvisioningViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(
        enabled = uiState.provisionerState is Connecting ||
                uiState.provisionerState is Connected ||
                uiState.provisionerState is Identifying ||
                uiState.provisionerState is Provisioning ||
                uiState.provisionerState is Disconnected
    ) {
        viewModel.disconnect()
    }
    ProvisionerScreen(
        uiState = uiState,
        beginProvisioning = viewModel::beginProvisioning,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        isValidAddress = viewModel::isValidAddress,
        onNetworkKeyClick = viewModel::onNetworkKeyClick,
        startProvisioning = viewModel::startProvisioning,
        authenticate = viewModel::authenticate,
        onProvisioningComplete = viewModel::onProvisioningComplete,
        onProvisioningFailed = viewModel::onProvisioningFailed,
        disconnect = viewModel::disconnect
    )
}

@Composable
private fun ProvisionerScreen(
    uiState: ProvisioningScreenUiState,
    beginProvisioning:(Context, BleScanResults) -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningParameters, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: () -> Unit,
    onProvisioningFailed: () -> Unit,
    disconnect: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDeviceCapabilitiesSheet by rememberSaveable { mutableStateOf(false) }
    val edgeToEdgeEnabled by remember { mutableStateOf(false) }
    val capabilitiesSheet = rememberModalBottomSheetState()
    var showAuthenticationDialog by remember { mutableStateOf(false) }

    ScannerSection(
        onDeviceFound = {
            beginProvisioning(context, it)
            scope.launch {
                capabilitiesSheet.expand()
                showDeviceCapabilitiesSheet = true
            }
        }
    )
    if (showDeviceCapabilitiesSheet) {
        val windowInsets = if (edgeToEdgeEnabled)
            WindowInsets(0) else BottomSheetDefaults.windowInsets
        ModalBottomSheet(
            onDismissRequest = {
                disconnect()
                showDeviceCapabilitiesSheet = false
            },
            sheetState = capabilitiesSheet,
            windowInsets = windowInsets
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        scope.launch {
                            capabilitiesSheet.hide()
                            delay(1000)
                            disconnect()
                            showDeviceCapabilitiesSheet = false
                        }
                    }) {
                        Icon(imageVector = Icons.Rounded.Close, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.label_device_information),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    TextButton(
                        enabled = uiState.provisionerState is Provisioning,
                        onClick = {
                            runCatching {
                                showAuthenticationDialog = true
                            }.onFailure {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = it.message
                                            ?: context.getString(R.string.label_unknown_error)
                                    )
                                }
                            }//.onSuccess {}
                        }) {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = stringResource(id = R.string.label_provision).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
                ProvisioningContent(
                    provisionerState = uiState.provisionerState,
                    snackbarHostState = snackbarHostState,
                    showAuthenticationDialog = showAuthenticationDialog,
                    onAuthenticationDialogDismissed = {
                        showAuthenticationDialog = false
                    },
                    onNameChanged = onNameChanged,
                    onAddressChanged = onAddressChanged,
                    isValidAddress = isValidAddress,
                    onNetworkKeyClick = onNetworkKeyClick,
                    startProvisioning = startProvisioning,
                    authenticate = authenticate,
                    onProvisioningComplete = {
                        showDeviceCapabilitiesSheet = false
                        onProvisioningComplete()
                    },
                    onProvisioningFailed = onProvisioningFailed
                )
            }
        }
    }
}

@Composable
private fun ScannerSection(onDeviceFound: (BleScanResults) -> Unit) {
    var unprovisionedDevice by remember {
        mutableStateOf<UnprovisionedDevice?>(null)
    }
    ScannerView(
        uuid = ParcelUuid(MeshProvisioningService.uuid),
        onResult = { result ->
            result.lastScanResult?.scanRecord?.bytes?.let { bytes ->
                unprovisionedDevice = UnprovisionedDevice.from(bytes.value)
            }?.let {
                onDeviceFound(result)
            }
        },
        deviceItem = {
            DeviceListItem(
                name = it.device.name,
                address = it.lastScanResult?.scanRecord?.bytes?.let { bytes ->
                    UnprovisionedDevice.from(bytes.value).uuid.toString().uppercase()
                } ?: it.device.address
            )
        },
        showFilter = false
    )
}

@Composable
private fun ProvisioningContent(
    provisionerState: ProvisionerState,
    snackbarHostState: SnackbarHostState,
    showAuthenticationDialog: Boolean,
    onAuthenticationDialogDismissed: (Boolean) -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningParameters, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: () -> Unit,
    onProvisioningFailed: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    when (provisionerState) {
        is Connecting -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_connecting_to,
                provisionerState.unprovisionedDevice.name
            )
        )

        is Connected -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_connected,
                provisionerState.unprovisionedDevice.name
            )
        )

        is Identifying -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_identifying,
                provisionerState.unprovisionedDevice.name
            )
        )

        is Provisioning -> ProvisioningStateInfo(
            state = provisionerState.state,
            unprovisionedDevice = provisionerState.unprovisionedDevice,
            snackbarHostState = snackbarHostState,
            showAuthenticationDialog = showAuthenticationDialog,
            onAuthenticationDialogDismissed = onAuthenticationDialogDismissed,
            onNameChanged = onNameChanged,
            onAddressChanged = onAddressChanged,
            isValidAddress = isValidAddress,
            onNetworkKeyClick = onNetworkKeyClick,
            authenticate = authenticate,
            onProvisioningComplete = onProvisioningComplete,
            onProvisioningFailed = onProvisioningFailed,
            onInputComplete = { scope.launch { sheetState.hide() } },
            startProvisioning = startProvisioning
        )

        is Error -> {
            var showAlertDialog by remember { mutableStateOf(true) }
            if (showAlertDialog) {
                MeshAlertDialog(
                    onDismissRequest = {
                        showAlertDialog = !showAlertDialog
                        onProvisioningFailed()
                    },
                    confirmButtonText = stringResource(id = R.string.label_ok),
                    onConfirmClick = {
                        showAlertDialog = !showAlertDialog
                        onProvisioningFailed()
                    },
                    dismissButtonText = null,
                    icon = Icons.Rounded.ErrorOutline,
                    iconColor = MaterialTheme.colorScheme.error,
                    title = stringResource(R.string.label_status),
                    text = when (provisionerState.throwable) {
                        is NodeAlreadyExists -> stringResource(
                            id = R.string.label_provisioning_completed_node_already_exists
                        )

                        else -> provisionerState.throwable.toString()
                    }
                )
            }

        }

        is Disconnected -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_disconnected,
                provisionerState.unprovisionedDevice.name
            ),
            isError = true,
            imageVector = Icons.Rounded.SentimentVeryDissatisfied
        )

        else -> {}
    }
}

@Composable
private fun ProvisioningStateInfo(
    state: ProvisioningState,
    unprovisionedDevice: UnprovisionedDevice,
    snackbarHostState: SnackbarHostState,
    showAuthenticationDialog: Boolean,
    onAuthenticationDialogDismissed: (Boolean) -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningParameters, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: () -> Unit,
    onProvisioningFailed: () -> Unit,
    onInputComplete: () -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit
) {
    when (state) {
        is ProvisioningState.RequestingCapabilities -> ProvisionerStateInfo(
            text = stringResource(id = R.string.label_provisioning_requesting_capabilities)
        )

        is ProvisioningState.CapabilitiesReceived -> DeviceCapabilities(
            state = state,
            snackbarHostState = snackbarHostState,
            unprovisionedDevice = unprovisionedDevice,
            showAuthenticationDialog = showAuthenticationDialog,
            onAuthenticationDialogDismissed = onAuthenticationDialogDismissed,
            onNameChanged = onNameChanged,
            onAddressChanged = onAddressChanged,
            isValidAddress = isValidAddress,
            onNetworkKeyClick = onNetworkKeyClick,
            startProvisioning = startProvisioning
        )

        is ProvisioningState.Provisioning -> ProvisionerStateInfo(
            text = stringResource(R.string.provisioning_in_progress)
        )

        is ProvisioningState.AuthActionRequired -> {
            ProvisionerStateInfo(
                text = stringResource(R.string.label_provisioning_authentication_required)
            )
            AuthenticationDialog(
                action = state.action,
                onOkClicked = authenticate,
                onCancelClicked = onProvisioningFailed
            )
        }

        ProvisioningState.InputComplete -> {
            ProvisionerStateInfo(
                text = stringResource(R.string.label_provisioning_authentication_completed)
            )
            onInputComplete()
        }

        is ProvisioningState.Failed -> {
            var showProvisioningFailed by remember { mutableStateOf(true) }
            if (showProvisioningFailed) {
                MeshAlertDialog(
                    onDismissRequest = {
                        showProvisioningFailed = !showProvisioningFailed
                        onProvisioningFailed()
                    },
                    confirmButtonText = stringResource(id = R.string.label_ok),
                    onConfirmClick = {
                        showProvisioningFailed = !showProvisioningFailed
                        onProvisioningFailed()
                    },
                    dismissButtonText = null,
                    icon = Icons.Rounded.ErrorOutline,
                    iconColor = MaterialTheme.colorScheme.nordicRed,
                    title = stringResource(R.string.label_status),
                    text = stringResource(R.string.label_provisioning_failed, state.error)
                )
            }
        }

        is ProvisioningState.Complete -> {
            var showProvisioningComplete by remember { mutableStateOf(true) }
            if (showProvisioningComplete) {
                MeshAlertDialog(
                    onDismissRequest = {
                        showProvisioningComplete = !showProvisioningComplete
                        onProvisioningComplete()
                    },
                    confirmButtonText = stringResource(id = R.string.label_ok),
                    onConfirmClick = {
                        showProvisioningComplete = !showProvisioningComplete
                        onProvisioningComplete()
                    },
                    dismissButtonText = null,
                    icon = Icons.Rounded.CheckCircleOutline,
                    iconColor = MaterialTheme.colorScheme.nordicLightGray,
                    title = stringResource(R.string.label_status),
                    text = stringResource(R.string.label_provisioning_completed)
                )
            }
        }
    }
}

@Composable
private fun ProvisionerStateInfo(
    text: String,
    shouldShowProgress: Boolean = true,
    isError: Boolean = false,
    imageVector: ImageVector = Icons.Rounded.Error,
    errorTint: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(fraction = 0.5f),
        horizontalAlignment = CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (shouldShowProgress && !isError) {
            CircularProgressIndicator()
        }
        if (isError) {
            Icon(
                modifier = Modifier.size(100.dp),
                imageVector = imageVector,
                contentDescription = null,
                tint = errorTint
            )
        }
        Spacer(modifier = Modifier.size(16.dp))
        Text(text = text)
    }
}

