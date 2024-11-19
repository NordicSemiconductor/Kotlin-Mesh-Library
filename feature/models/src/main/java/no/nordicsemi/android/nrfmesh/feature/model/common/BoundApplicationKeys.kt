package no.nordicsemi.android.nrfmesh.feature.model.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddLink
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.NotStarted.isInProgress
import no.nordicsemi.android.nrfmesh.core.ui.BottomSheetTopAppBar
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.EmptyNetworkKeysContent
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.BottomSheetApplicationKeys
import no.nordicsemi.android.nrfmesh.feature.configurationserver.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigBeaconGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelAppBind
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Model
import java.lang.IllegalStateException
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BoundApplicationKeys(
    model: Model,
    messageState: MessageState,
    send: (AcknowledgedConfigMessage) -> Unit,
    navigateToApplicationKey: (UUID) -> Unit,
    navigateToApplicationKeys: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val keys by remember {
        mutableStateOf(model.parentElement?.parentNode?.applicationKeys ?: emptyList())
    }
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.AddLink,
        title = stringResource(R.string.label_bind_application_keys),
        subtitle = "${model.boundApplicationKeys.size} key(s) are bound",
        body = {

        }
    ) {
        OutlinedButton(
            enabled = !messageState.isInProgress(),
            onClick = { send(ConfigBeaconGet()) },
            content = { Text(text = stringResource(R.string.label_list)) }
        )
        OutlinedButton(
            modifier = Modifier.padding(start = 8.dp),
            enabled = !messageState.isInProgress(),
            onClick = { showBottomSheet = !showBottomSheet },
            content = { Text(text = stringResource(R.string.label_bind)) }
        )
    }

    if (showBottomSheet) {
        BottomSheetApplicationKeys(
            bottomSheetState = bottomSheetState,
            title = stringResource(R.string.label_bind_key),
            keys = keys,
            onAppKeyClicked = {
                send(ConfigModelAppBind(model = model, applicationKey = it))
                scope.launch {
                    bottomSheetState.hide()
                }.invokeOnCompletion {
                    if(showBottomSheet) showBottomSheet = false
                }
            },
            navigateToNetworkKeys = navigateToApplicationKeys,
            onDismissClick = { showBottomSheet = !showBottomSheet },
            emptyKeysContent = {
                EmptyNetworkKeysContent(
                    noItemsAvailableContent = {
                        MeshNoItemsAvailable(
                            imageVector = Icons.Outlined.VpnKey,
                            title = stringResource(R.string.label_no_app_keys_to_bind)
                        )
                    },
                    onClickText = stringResource(R.string.action_settings),
                    onClick = navigateToApplicationKeys
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
private fun BottomSheetKeys(
    model: Model,
    onAddKeyClicked: (ApplicationKey) -> Unit,
    navigateToApplicationKeys: (UUID) -> Unit,
    onDismissClick: () -> Unit
) {
    val keys by remember {
        mutableStateOf(model.parentElement?.parentNode?.applicationKeys ?: emptyList())
    }
    ModalBottomSheet(onDismissRequest = onDismissClick) {
        BottomSheetTopAppBar(
            navigationIcon = Icons.Outlined.Close,
            onNavigationIconClick = onDismissClick,
            title = stringResource(R.string.label_bind_key)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            if (keys.isEmpty()) {
                item {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.VpnKey,
                        title = stringResource(R.string.label_no_app_keys_to_bind)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        OutlinedButton(
                            onClick = {
                                navigateToApplicationKeys(
                                    model.parentElement?.parentNode?.uuid
                                        ?: throw IllegalStateException("UUID not found")
                                )
                            },
                            content = { Text(text = stringResource(R.string.label_add)) }
                        )
                    }
                }
            } else {
                items(items = keys) { key ->
                    ElevatedCardItem(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        onClick = {
                            onDismissClick()
                            onAddKeyClicked(key)
                        },
                        imageVector = Icons.Outlined.VpnKey,
                        title = key.name,
                        subtitle = key.key.toHexString()
                    )
                }
            }
        }
    }
}
