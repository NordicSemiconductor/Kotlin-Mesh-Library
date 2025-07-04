package no.nordicsemi.android.nrfmesh.feature.model.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.MessageState
import no.nordicsemi.android.nrfmesh.core.common.fixedGroupAddressesForSubscriptions
import no.nordicsemi.android.nrfmesh.core.common.name
import no.nordicsemi.android.nrfmesh.core.common.unsubscribedGroups
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.feature.models.R
import no.nordicsemi.kotlin.mesh.core.messages.AcknowledgedConfigMessage
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionDeleteAll
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionVirtualAddressAdd
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigModelSubscriptionVirtualAddressDelete
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigSigModelSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.messages.foundation.configuration.ConfigVendorModelSubscriptionGet
import no.nordicsemi.kotlin.mesh.core.model.AllNodes
import no.nordicsemi.kotlin.mesh.core.model.FixedGroupAddress
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.SubscriptionAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
internal fun Subscriptions(
    snackbarHostState: SnackbarHostState,
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
            content = {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null
                )
            }
        )
        IconButton(
            onClick = { showDeleteAllDialog = true },
            content = {
                Icon(
                    imageVector = Icons.Outlined.DeleteSweep,
                    contentDescription = null
                )
            }
        )
        IconButton(
            onClick = { showBottomSheet = true },
            content = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) }
        )
    }
    if (model.subscribe.isNotEmpty()) {
        model.subscribe.forEach {
            // A key is used to ensure that the items are stable and do not cause recomposition
            // issues.
            key(it.address) {
                SubscriptionRow(
                    scope = scope,
                    snackbarHostState = snackbarHostState,
                    model = model,
                    address = it,
                    send = send
                )
            }
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
                val addresses = model.unsubscribedGroups() + fixedGroupAddressesForSubscriptions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    item {
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
                    }
                    itemsIndexed(
                        items = addresses,
                        key = { index, item -> index }
                    ) { index, item ->
                        if (item is Group) {
                            ElevatedCardItem(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                imageVector = Icons.Outlined.GroupWork,
                                title = item.name,
                                onClick = {
                                    scope.launch {
                                        bottomSheetState.hide()
                                    }.invokeOnCompletion {
                                        if (!bottomSheetState.isVisible) {
                                            showBottomSheet = false
                                        }
                                    }
                                    send(
                                        if (item.address is VirtualAddress) {
                                            ConfigModelSubscriptionVirtualAddressAdd(
                                                group = item,
                                                model = model
                                            )
                                        } else {
                                            ConfigModelSubscriptionAdd(group = item, model = model)
                                        }
                                    )
                                }
                            )
                        } else {
                            if (model.parentElement?.isPrimary == false && item !is AllNodes) {
                                ElevatedCardItem(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    imageVector = Icons.Outlined.GroupWork,
                                    title = (item as FixedGroupAddress).name(),
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
                                                address = item as SubscriptionAddress,
                                                model = model
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                    item {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .padding(top = 16.dp),
                            text = stringResource(R.string.label_subscribe_addresses_rationale),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubscriptionRow(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    model: Model,
    address: SubscriptionAddress,
    send: (AcknowledgedConfigMessage) -> Unit,
) {
    val context = LocalContext.current
    var shouldDismiss by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            shouldDismiss = handleValueChange(model = model, address = address)
            if (!shouldDismiss) {
                showSnackbar = true
            }
            shouldDismiss
        },
        positionalThreshold = { it * 0.5f }
    )
    SwipeDismissItem(
        dismissState = dismissState,
        content = {
            ElevatedCardItem(
                imageVector = Icons.Outlined.GroupWork,
                title = model.parentElement?.parentNode?.network
                    ?.group(address = address.address)
                    ?.name
                    ?: (address as? FixedGroupAddress)?.name()
                    ?: address.toHexString(),
            )
        }
    )
    if (shouldDismiss) {
        LaunchedEffect(snackbarHostState) {
            scope.launch {
                send(
                    if (address is VirtualAddress) {
                        ConfigModelSubscriptionVirtualAddressDelete(
                            elementAddress = model.parentElement?.unicastAddress!!,
                            address = address,
                            model = model
                        )
                    } else {
                        ConfigModelSubscriptionDelete(
                            elementAddress = model.parentElement?.unicastAddress!!,
                            address = address.address,
                            model = model
                        )
                    }
                )
            }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(snackbarHostState) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.label_primary_element_subscription_error)
                ).also {
                    if (it == SnackbarResult.Dismissed) {
                        showSnackbar = false
                    }
                }
            }
        }
    }
}

private fun handleValueChange(model: Model, address: SubscriptionAddress) =
    when (model.parentElement?.isPrimary == true) {
        true -> address !is AllNodes
        else -> true
    }