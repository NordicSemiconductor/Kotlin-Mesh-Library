@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.ui.provisioning

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.theme.nordicLightGray
import no.nordicsemi.android.common.theme.nordicRed
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisionerState
import no.nordicsemi.android.nrfmesh.viewmodel.ProvisioningViewModel
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.provisioning.AuthAction
import no.nordicsemi.kotlin.mesh.provisioning.AuthenticationMethod
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningConfiguration
import no.nordicsemi.kotlin.mesh.provisioning.ProvisioningState
import no.nordicsemi.kotlin.mesh.provisioning.UnprovisionedDevice

@Composable
fun ProvisioningRoute1(viewModel: ProvisioningViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(
        enabled = uiState.provisionerState is ProvisionerState.Connecting ||
                uiState.provisionerState is ProvisionerState.Connected ||
                uiState.provisionerState is ProvisionerState.Identifying ||
                uiState.provisionerState is ProvisionerState.Provisioning ||
                uiState.provisionerState is ProvisionerState.Disconnected
    ) {
        viewModel.disconnect()
    }
    ProvisioningScreen(
        unprovisionedDevice = uiState.unprovisionedDevice,
        provisionerState = uiState.provisionerState,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        isValidAddress = viewModel::isValidAddress,
        onNetworkKeyClick = viewModel::onNetworkKeyClick,
        startProvisioning = viewModel::startProvisioning,
        authenticate = viewModel::authenticate,
        onProvisioningComplete = viewModel::onProvisioningComplete,
        onProvisioningFailed = viewModel::onProvisioningFailed
    )
}

@Composable
private fun ProvisioningScreen(
    provisionerState: ProvisionerState,
    unprovisionedDevice: UnprovisionedDevice,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningConfiguration, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: () -> Unit,
    onProvisioningFailed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    when (provisionerState) {
        is ProvisionerState.Connecting -> ProvisionerStateInfo(
            text = stringResource(R.string.label_connecting, unprovisionedDevice.name)
        )

        is ProvisionerState.Connected -> ProvisionerStateInfo(
            text = stringResource(R.string.label_connected, unprovisionedDevice.name)
        )

        ProvisionerState.Identifying -> ProvisionerStateInfo(
            text = stringResource(R.string.label_identifying, unprovisionedDevice.name)
        )

        is ProvisionerState.Provisioning -> ProvisioningStateInfo(
            unprovisionedDevice = unprovisionedDevice,
            state = provisionerState.state,
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

        is ProvisionerState.Error -> {
            ProvisionerStateInfo(
                text = provisionerState.throwable.message
                    ?: stringResource(id = R.string.label_unknown_error)
            )
        }

        is ProvisionerState.Disconnected -> ProvisionerStateInfo(
            text = stringResource(R.string.label_disconnected, unprovisionedDevice.name),
            isError = true,
            imageVector = Icons.Rounded.SentimentVeryDissatisfied
        )
    }
}

@Composable
private fun ProvisioningStateInfo(
    state: ProvisioningState,
    unprovisionedDevice: UnprovisionedDevice,
    onNameChanged: (String) -> Unit,
    onAddressChanged: (ProvisioningConfiguration, Int, Int) -> Result<Boolean>,
    isValidAddress: (UShort) -> Boolean,
    onNetworkKeyClick: (KeyIndex) -> Unit,
    authenticate: (AuthAction, String) -> Unit,
    onProvisioningComplete: () -> Unit,
    onProvisioningFailed: () -> Unit,
    onInputComplete: () -> Unit,
    startProvisioning: (AuthenticationMethod) -> Unit
) {
    BackHandler(enabled = state is ProvisioningState.AuthActionRequired) {}
    when (state) {
        is ProvisioningState.RequestingCapabilities -> ProvisionerStateInfo(
            text = stringResource(id = R.string.label_provisioning_requesting_capabilities)
        )

        is ProvisioningState.CapabilitiesReceived -> DeviceCapabilities(
            state = state,
            unprovisionedDevice = unprovisionedDevice,
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
            ProvisionerStateInfo(text = stringResource(R.string.label_provisioning_authentication_required))
            OobActionSelectionBottomSheet(
                action = state.action,
                onOkClicked = authenticate,
                onDismissRequest = {  },
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

