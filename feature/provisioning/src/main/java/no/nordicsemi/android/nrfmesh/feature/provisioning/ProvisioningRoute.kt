@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioning

import android.content.Context
import android.os.ParcelUuid
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.SentimentVeryDissatisfied
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.nordicLightGray
import no.nordicsemi.android.common.theme.nordicRed
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.ui.scanner.ScannerView
import no.nordicsemi.android.kotlin.ble.ui.scanner.WithServiceUuid
import no.nordicsemi.android.kotlin.ble.ui.scanner.main.DeviceListItem
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Error
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningScreen
import no.nordicsemi.kotlin.mesh.core.exception.NodeAlreadyExists
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningParameters
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice

@Composable
internal fun ProvisioningRoute(
    appState: AppState,
    uiState: ProvisioningScreenUiState,
    beginProvisioning: (Context, BleScanResults) -> Unit,
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
    val screen = appState.currentScreen as? ProvisioningScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                ProvisioningScreen.Actions.BACK -> disconnect()
            }
        }?.launchIn(this)
    }

    BackHandler(
        enabled = uiState.provisionerState is ProvisionerState.Connecting ||
                uiState.provisionerState is ProvisionerState.Connected ||
                uiState.provisionerState is ProvisionerState.Identifying ||
                uiState.provisionerState is ProvisionerState.Provisioning ||
                uiState.provisionerState is ProvisionerState.Disconnected
    ) {
        disconnect()
    }
    ProvisionerScreen(
        uiState = uiState,
        beginProvisioning = beginProvisioning,
        onNameChanged = onNameChanged,
        onAddressChanged = onAddressChanged,
        isValidAddress = isValidAddress,
        onNetworkKeyClick = onNetworkKeyClick,
        startProvisioning = startProvisioning,
        authenticate = authenticate,
        onProvisioningComplete = onProvisioningComplete,
        onProvisioningFailed = onProvisioningFailed,
        disconnect = disconnect
    )
}

@Composable
private fun ProvisionerScreen(
    uiState: ProvisioningScreenUiState,
    beginProvisioning: (Context, BleScanResults) -> Unit,
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
    var openDeviceCapabilitiesSheet by rememberSaveable { mutableStateOf(false) }
    val capabilitiesSheetState = rememberModalBottomSheetState()
    var showAuthenticationDialog by remember { mutableStateOf(false) }

    ScannerSection(
        onDeviceFound = {
            beginProvisioning(context, it)
            openDeviceCapabilitiesSheet = true
        }
    )
    if (openDeviceCapabilitiesSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                disconnect()
                openDeviceCapabilitiesSheet = false
            },
            sheetState = capabilitiesSheetState
        ) {
            BottomSheetTopAppBar(
                title = stringResource(R.string.label_device_information),
                titleStyle = MaterialTheme.typography.titleLarge,
                actions = {
                    Spacer(modifier = Modifier.size(16.dp))
                    TextButton(
                        enabled = uiState.provisionerState is ProvisionerState.Provisioning,
                        onClick = {
                            runCatching { showAuthenticationDialog = true }
                                .onFailure {
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
            )
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
                onProvisioningComplete = onProvisioningComplete,
                onProvisioningFailed = onProvisioningFailed,
                dismissCapabilitiesSheet = { scope.launch { capabilitiesSheetState.hide() } }
            )
        }
    }
}

@Composable
private fun ScannerSection(onDeviceFound: (BleScanResults) -> Unit) {
    var unprovisionedDevice by remember {
        mutableStateOf<UnprovisionedDevice?>(null)
    }
    val filters = listOf(
        WithServiceUuid(
            title = "Unprovisioned",
            uuid = ParcelUuid(MeshProvisioningService.uuid),
            initiallySelected = false
        )
    )
    ScannerView(
        filters = filters,
        onResult = { result ->
            result.lastScanResult?.scanRecord?.bytes?.let { bytes ->
                unprovisionedDevice = UnprovisionedDevice.from(bytes.value)
            }?.let {
                onDeviceFound(result)
            }
        },
        deviceItem = {
            DeviceListItem(
                modifier = Modifier.padding(vertical = 16.dp),
                name = it.device.name,
                address = /*it.lastScanResult?.scanRecord?.bytes?.let { bytes ->
                    UnprovisionedDevice.from(bytes.value).uuid.toString().uppercase()
                } ?:*/ it.device.address
            )
        },
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
    dismissCapabilitiesSheet: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    when (provisionerState) {
        is ProvisionerState.Connecting -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_connecting_to,
                provisionerState.unprovisionedDevice.name
            )
        )

        is ProvisionerState.Connected -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_connected,
                provisionerState.unprovisionedDevice.name
            )
        )

        is ProvisionerState.Identifying -> ProvisionerStateInfo(
            text = stringResource(
                R.string.label_identifying,
                provisionerState.unprovisionedDevice.name
            )
        )

        is ProvisionerState.Provisioning -> ProvisioningStateInfo(
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
            startProvisioning = startProvisioning,
            dismissCapabilitiesSheet = dismissCapabilitiesSheet
        )

        is Error -> {
            dismissCapabilitiesSheet()
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

        is ProvisionerState.Disconnected -> ProvisionerStateInfo(
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
    startProvisioning: (AuthenticationMethod) -> Unit,
    dismissCapabilitiesSheet: () -> Unit
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
            var showAlertDialog by remember { mutableStateOf(true) }
            if (showAlertDialog) {
                dismissCapabilitiesSheet()
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
                    iconColor = MaterialTheme.colorScheme.nordicRed,
                    title = stringResource(R.string.label_status),
                    text = stringResource(R.string.label_provisioning_failed, state.error)
                )
            }
        }

        is ProvisioningState.Complete -> {
            var showAlertDialog by remember { mutableStateOf(true) }
            if (showAlertDialog) {
                dismissCapabilitiesSheet()
                MeshAlertDialog(
                    onDismissRequest = {
                        showAlertDialog = !showAlertDialog
                        onProvisioningComplete()
                    },
                    confirmButtonText = stringResource(id = R.string.label_ok),
                    onConfirmClick = {
                        showAlertDialog = !showAlertDialog
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

