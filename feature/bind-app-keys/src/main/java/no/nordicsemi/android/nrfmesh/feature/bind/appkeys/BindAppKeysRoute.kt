package no.nordicsemi.android.nrfmesh.feature.bind.appkeys

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.feature.bind.appkeys.navigation.BoundAppKeysScreen
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.BottomSheetApplicationKeys
import no.nordicsemi.kotlin.data.toHexString
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelAppBind
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Model
import java.util.UUID

@Composable
internal fun BindAppKeysRoute(
    appState: AppState,
    uiState: BindAppKeysScreenUiState,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    onBackPressed: () -> Unit
) {
    val screen = appState.currentScreen as? BoundAppKeysScreen
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach {
            when (it) {
                BoundAppKeysScreen.Actions.BACK -> onBackPressed()
                BoundAppKeysScreen.Actions.BIND_KEY -> showBottomSheet = true
            }
        }?.launchIn(this)
    }

    BindAppKeysScreen(
        uiState = uiState,
        showBottomSheet = showBottomSheet,
        navigateToConfigApplicationKeys = navigateToConfigApplicationKeys,
        send = send,
        onBottomSheetDismissed = { showBottomSheet = false }
    )
}

@Composable
private fun BindAppKeysScreen(
    uiState: BindAppKeysScreenUiState,
    showBottomSheet: Boolean,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    onBottomSheetDismissed: () -> Unit
) {

    when (uiState.modelState) {
        ModelState.Loading -> {}
        is ModelState.Success -> BoundKeys(
            model = uiState.modelState.model,
            boundAppKeys = uiState.boundKeys,
            addedKeys = uiState.addedKeys,
            navigateToConfigApplicationKeys = navigateToConfigApplicationKeys,
            send = send,
            showBottomSheet = showBottomSheet,
            onBottomSheetDismissed = onBottomSheetDismissed
        )

        is ModelState.Error -> {}
    }
}

@OptIn(ExperimentalStdlibApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BoundKeys(
    model: Model,
    boundAppKeys: List<ApplicationKey>,
    addedKeys: List<ApplicationKey>,
    navigateToConfigApplicationKeys: (UUID) -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
    showBottomSheet: Boolean,
    onBottomSheetDismissed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        if (boundAppKeys.isEmpty()) {
            item {
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.VpnKey,
                    title = stringResource(R.string.label_no_bound_app_keys),
                    rationale = stringResource(R.string.label_bind_an_app_key_rationale)
                )
            }
        } else {
            items(items = boundAppKeys) { key ->
                ElevatedCardItem(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onClick = onBottomSheetDismissed,
                    imageVector = Icons.Outlined.VpnKey,
                    title = key.name,
                    subtitle = key.key.toHexString()
                )
            }
        }
    }
    if (showBottomSheet) {
        BottomSheetApplicationKeys(
            bottomSheetState = bottomSheetState,
            title = stringResource(R.string.label_bind_key),
            keys = addedKeys,
            onAppKeyClicked = {
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    if (showBottomSheet) {
                        onBottomSheetDismissed()
                    }
                }
                send(ConfigModelAppBind(model = model, applicationKey = it))
            },
            onDismissClick = onBottomSheetDismissed,
            emptyKeysContent = {
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.VpnKey,
                    title = stringResource(R.string.label_no_app_keys_to_bind),
                    onClickText = stringResource(R.string.label_add_key),
                    onClick = {
                        navigateToConfigApplicationKeys(
                            model.parentElement?.parentNode?.uuid
                                ?: throw IllegalArgumentException("Parent node UUID is null")
                        )
                    }
                )
            }
        )
    }
}