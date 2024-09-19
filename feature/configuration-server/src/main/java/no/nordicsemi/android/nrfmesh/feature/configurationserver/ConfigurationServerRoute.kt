package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups3
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.SwitchWithIcon
import no.nordicsemi.android.nrfmesh.feature.configurationserver.navigation.ConfigurationServerScreen
import no.nordicsemi.kotlin.mesh.core.model.FeatureState
import no.nordicsemi.kotlin.mesh.core.model.Friend
import no.nordicsemi.kotlin.mesh.core.model.Proxy

@Composable
internal fun ConfigurationServerRoute(
    appState: AppState,
    uiState: ModelScreenUiState,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetProxyStateClicked: () -> Unit,
    onGetFriendStateClicked: () -> Unit,
    onFriendStateToggled: (Boolean) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? ConfigurationServerScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach {
            when (it) {
                ConfigurationServerScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }

    ConfigurationServerModelScreen(
        uiState = uiState,
        onGetProxyStateClicked = onGetProxyStateClicked,
        onProxyStateToggled = onProxyStateToggled,
        onGetFriendStateClicked = onGetFriendStateClicked,
        onFriendStateToggled = onFriendStateToggled
    )
}

@Composable
internal fun ConfigurationServerModelScreen(
    uiState: ModelScreenUiState,
    onGetProxyStateClicked: () -> Unit,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetFriendStateClicked: () -> Unit,
    onFriendStateToggled: (Boolean) -> Unit
) {
    when (uiState.modelState) {
        ModelState.Loading -> {}
        is ModelState.Success -> ConfigurationServerModel(
            proxy = uiState.modelState.model.parentElement?.parentNode?.features?.proxy,
            onGetProxyStateClicked = onGetProxyStateClicked,
            onProxyStateToggled = onProxyStateToggled,
            friend = uiState.modelState.model.parentElement?.parentNode?.features?.friend,
            onGetFriendStateClicked = onGetFriendStateClicked,
            onFriendStateToggled = onFriendStateToggled
        )

        is ModelState.Error -> TODO()
    }
}

@Composable
internal fun ConfigurationServerModel(
    proxy: Proxy?,
    onGetProxyStateClicked: () -> Unit,
    onProxyStateToggled: (Boolean) -> Unit,
    friend: Friend?,
    onGetFriendStateClicked: () -> Unit,
    onFriendStateToggled: (Boolean) -> Unit
) {
    ProxyStateRow(
        proxy = proxy,
        onProxyStateToggled = onProxyStateToggled,
        onGetProxyStateClicked = onGetProxyStateClicked,
    )
    FriendFeature(
        friend = friend,
        onFriendStateToggled = onFriendStateToggled,
        onGetFriendStateClicked = onGetFriendStateClicked
    )
}

@Composable
private fun ProxyStateRow(
    proxy: Proxy?,
    onProxyStateToggled: (Boolean) -> Unit,
    onGetProxyStateClicked: () -> Unit
) {
    var enabled by rememberSaveable {
        mutableStateOf(proxy?.state?.let { it == FeatureState.Enabled } ?: false)
    }
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onGetProxyStateClicked),
        imageVector = Icons.Outlined.Hub,
        title = stringResource(R.string.label_gatt_proxy),
        titleAction = {
            SwitchWithIcon(isChecked = enabled, onCheckedChange = {
                enabled = it
                if (!it) {
                    showProxyStateDialog = !showProxyStateDialog
                } else {
                    onProxyStateToggled(true)
                }
            })
        },
        subtitle = "Proxy state is ${if (enabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(onClick = onGetProxyStateClicked) {
            Text(text = stringResource(R.string.label_get_state))
        }
    }
    if (showProxyStateDialog) {
        MeshAlertDialog(onDismissRequest = {
            showProxyStateDialog = !showProxyStateDialog
            enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
        },
            icon = Icons.Outlined.Hub,
            title = stringResource(R.string.label_disable_proxy_feature),
            text = stringResource(R.string.label_are_you_sure_rationale),
            iconColor = Color.Red,
            onConfirmClick = {
                enabled = false
                onProxyStateToggled(false)
                showProxyStateDialog = !showProxyStateDialog
            },
            onDismissClick = {
                showProxyStateDialog = !showProxyStateDialog
                enabled = proxy?.state?.let { it == FeatureState.Enabled } ?: false
            })
    }
}

@Composable
private fun FriendFeature(
    friend: Friend?,
    onFriendStateToggled: (Boolean) -> Unit,
    onGetFriendStateClicked: () -> Unit
) {
    var enabled by rememberSaveable {
        mutableStateOf(friend?.state?.let { it == FeatureState.Enabled } ?: false)
    }
    var showProxyStateDialog by rememberSaveable { mutableStateOf(false) }
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = onGetFriendStateClicked),
        imageVector = Icons.Outlined.Groups3,
        title = stringResource(R.string.label_friend_feature),
        titleAction = {
            SwitchWithIcon(isChecked = enabled, onCheckedChange = {
                enabled = it
                if (!it) {
                    showProxyStateDialog = !showProxyStateDialog
                } else {
                    onFriendStateToggled(true)
                }
            })
        },
        subtitle = "Friend feature is ${if (enabled) "enabled" else "disabled"}",
        supportingText = stringResource(R.string.label_proxy_state_rationale)
    ) {
        OutlinedButton(onClick = onGetFriendStateClicked) {
            Text(text = stringResource(R.string.label_get_state))
        }
    }
}