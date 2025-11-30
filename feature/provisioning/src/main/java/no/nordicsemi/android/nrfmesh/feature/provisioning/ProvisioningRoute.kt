@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioning

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SyncLock
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
import kotlinx.coroutines.launch
import no.nordicsemi.kotlin.mesh.bearer.gatt.utils.MeshProvisioningService
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.provisioning.ProvisionerState.Error
import no.nordicsemi.android.nrfmesh.feature.scanner.ScannerContent
import no.nordicsemi.kotlin.ble.client.android.ScanResult
import no.nordicsemi.kotlin.mesh.core.exception.NodeAlreadyExists
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningParameters
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice
import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun ProvisioningRoute(
    uiState: ProvisioningScreenUiState,
    beginProvisioning: (ScanResult) -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningParameters, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: (Uuid) -> Unit,
    onProvisioningFailed: () -> Unit,
    disconnect: () -> Unit,
) {
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
private fun ProvisionerScreen(
    uiState: ProvisioningScreenUiState,
    beginProvisioning: (ScanResult) -> Unit,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningParameters, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: (Uuid) -> Unit,
    onProvisioningFailed: () -> Unit,
    disconnect: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var openDeviceCapabilitiesSheet by rememberSaveable { mutableStateOf(false) }
    val capabilitiesSheetState = rememberModalBottomSheetState()
    var showAuthenticationDialog by remember { mutableStateOf(false) }

    ScannerSection(
        network = uiState.meshNetwork,
        onScanResultSelected = {
            beginProvisioning(it)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle(
                        modifier = Modifier.weight(weight = 1f),
                        title = stringResource(R.string.label_device_information),
                    )
                    MeshOutlinedButton(
                        modifier = Modifier.padding(end = 16.dp),
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
                                }
                        },
                        buttonIcon = Icons.Outlined.SyncLock,
                        text = stringResource(id = R.string.label_provision)
                    )
                }
                ProvisioningContent(
                    provisionerState = uiState.provisionerState,
                    snackbarHostState = snackbarHostState,
                    showAuthenticationDialog = showAuthenticationDialog,
                    onAuthenticationDialogDismissed = { showAuthenticationDialog = false },
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
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun ScannerSection(network: MeshNetwork?, onScanResultSelected: (ScanResult) -> Unit) {
    ScannerContent(
        meshNetwork = network,
        service = MeshProvisioningService,
        onScanResultSelected = onScanResultSelected
    )
}

@OptIn(ExperimentalUuidApi::class)
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
    onProvisioningComplete: (Uuid) -> Unit,
    onProvisioningFailed: () -> Unit,
    dismissCapabilitiesSheet: () -> Unit,
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

@OptIn(ExperimentalUuidApi::class)
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
    onProvisioningComplete: (Uuid) -> Unit,
    onProvisioningFailed: () -> Unit,
    onInputComplete: () -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    dismissCapabilitiesSheet: () -> Unit,
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
                    iconColor = MaterialTheme.colorScheme.error,
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
                        onProvisioningComplete(unprovisionedDevice.uuid)
                    },
                    confirmButtonText = stringResource(id = R.string.label_ok),
                    onConfirmClick = {
                        showAlertDialog = !showAlertDialog
                        onProvisioningComplete(unprovisionedDevice.uuid)
                    },
                    dismissButtonText = null,
                    icon = Icons.Rounded.CheckCircleOutline,
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
    errorTint: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
) {
    Column(
        modifier = Modifier.fillMaxSize(),
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

