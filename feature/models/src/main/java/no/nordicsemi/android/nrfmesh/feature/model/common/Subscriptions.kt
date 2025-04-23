package no.nordicsemi.android.nrfmesh.feature.model.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.name
import no.nordicsemi.android.nrfmesh.core.common.fixedGroupAddressesForSubscriptions
import no.nordicsemi.android.nrfmesh.core.common.unsubscribedGroups
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionDeleteAll
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionVirtualAddressAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigSigModelSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigVendorModelSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.SubscriptionAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Subscriptions(
    messageState: MessageState,
    model: Model,
    navigateToGroups: () -> Unit,
    send: (AcknowledgedConfigMessage) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(
            modifier = Modifier.weight(weight = 1f),
            title = stringResource(R.string.label_subscriptions)
        )
        IconButton(
            onClick = {
                when (model.modelId) {
                    is SigModelId -> send(ConfigSigModelSubscriptionGet(model = model))
                    is VendorModelId -> send(ConfigVendorModelSubscriptionGet(model = model))
                }
            },
            content = { Icon(imageVector = Icons.Outlined.Refresh, contentDescription = null) }
        )
        IconButton(
            onClick = { showDeleteAllDialog = true },
            content = { Icon(imageVector = Icons.Outlined.DeleteSweep, contentDescription = null) }
        )
        IconButton(
            onClick = { showBottomSheet = true },
            content = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) }
        )
    }
    if (model.subscribe.isNotEmpty()) {
        model.subscribe.forEach {
            val dismissState = rememberSwipeToDismissBoxState()
            SwipeDismissItem(
                dismissState = dismissState,
                content = {
                    ElevatedCardItem(
                        imageVector = Icons.Outlined.GroupWork,
                        title = model.parentElement?.parentNode?.network
                            ?.group(address = it.address)
                            ?.name
                            ?: (it as? FixedGroupAddress)?.name()
                            ?: it.toHexString(),
                    )
                }
            )
        }
    } else {
        ElevatedCardItem(
            modifier = Modifier.padding(horizontal = 16.dp),
            imageVector = Icons.Outlined.GroupWork,
            title = stringResource(R.string.label_no_subscriptions)
        )
    }

    if (showDeleteAllDialog) {
        MeshAlertDialog(
            icon = Icons.Outlined.DeleteSweep,
            iconColor = Color.Red,
            title = stringResource(R.string.label_delete_all_subscriptions),
            text = stringResource(R.string.label_delete_all_subscriptions_rationale),
            onConfirmClick = {
                showDeleteAllDialog = false
                send(ConfigModelSubscriptionDeleteAll(model = model))
            },
            onDismissClick = { showDeleteAllDialog = false },
            onDismissRequest = { showDeleteAllDialog = false }
        )
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { showBottomSheet = false },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp)
                        .verticalScroll(state = rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(
                            modifier = Modifier.weight(weight = 1f),
                            title = stringResource(R.string.label_select_subscribe_group)
                        )
                        MeshOutlinedButton(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            enabled = !messageState.isInProgress(),
                            onClick = navigateToGroups,
                            isOnClickActionInProgress = messageState.isInProgress() &&
                                    messageState.message is ConfigModelSubscriptionAdd,
                            buttonIcon = Icons.Outlined.Add,
                            text = stringResource(R.string.label_add_group)
                        )
                    }
                    model.unsubscribedGroups().forEach { group ->
                        ElevatedCardItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            imageVector = Icons.Outlined.GroupWork,
                            title = group.name,
                            onClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    if (!bottomSheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                                send(
                                    if (group.address is VirtualAddress) {
                                        ConfigModelSubscriptionVirtualAddressAdd(
                                            group = group,
                                            model = model
                                        )
                                    } else {
                                        ConfigModelSubscriptionAdd(group = group, model = model)
                                    }
                                )
                            }
                        )
                    }
                    fixedGroupAddressesForSubscriptions.forEach { address ->
                        ElevatedCardItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            imageVector = Icons.Outlined.GroupWork,
                            title = address.name(),
                            onClick = {
                                scope.launch {
                                    bottomSheetState.hide()
                                }.invokeOnCompletion {
                                    if (!bottomSheetState.isVisible) {
                                        showBottomSheet = false
                                    }
                                }
                                send(
                                    ConfigModelSubscriptionAdd(
                                        address = address as SubscriptionAddress,
                                        model = model
                                    )
                                )
                            }
                        )
                    }

                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 16.dp),
                        text = stringResource(R.string.label_subscribe_addresses_rationale),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        )
    }
}
