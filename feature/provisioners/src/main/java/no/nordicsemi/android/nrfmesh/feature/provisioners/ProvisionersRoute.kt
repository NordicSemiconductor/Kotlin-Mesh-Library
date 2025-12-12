@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.feature.provisioners

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.SwipeDismissItem
import no.nordicsemi.android.nrfmesh.core.ui.isDismissed
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun ProvisionersRoute(
    highlightSelectedItem: Boolean,
    provisioners: List<ProvisionerData>,
    onAddProvisionerClicked: () -> Provisioner,
    onSwiped: (ProvisionerData) -> Unit,
    onUndoClicked: (ProvisionerData) -> Unit,
    remove: (ProvisionerData) -> Unit,
    navigateToProvisioner: (Uuid) -> Unit,
) {
    Provisioners(
        highlightSelectedItem = highlightSelectedItem,
        provisioners = provisioners,
        addProvisioner = onAddProvisionerClicked,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove,
        navigateToProvisioner = navigateToProvisioner
    )
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun Provisioners(
    highlightSelectedItem: Boolean,
    provisioners: List<ProvisionerData>,
    addProvisioner: () -> Provisioner,
    onSwiped: (ProvisionerData) -> Unit,
    onUndoClicked: (ProvisionerData) -> Unit,
    remove: (ProvisionerData) -> Unit,
    navigateToProvisioner: (Uuid) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedUuid by rememberSaveable { mutableStateOf<String?>(null) }
    Scaffold(
        modifier = Modifier.background(color = Color.Red),
        contentWindowInsets = WindowInsets(top = 8.dp),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                text = { Text(text = stringResource(R.string.label_add_provisioner)) },
                icon = { Icon(imageVector = Icons.Outlined.Add, contentDescription = null) },
                onClick = {
                    runCatching {
                        addProvisioner()
                    }.onSuccess {
                        selectedUuid = it.uuid.toString()
                        navigateToProvisioner(it.uuid)
                    }
                },
                expanded = true
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .consumeWindowInsets(paddingValues = paddingValues)
        ) {
            when (provisioners.isEmpty()) {
                true -> MeshNoItemsAvailable(
                    modifier = Modifier.fillMaxSize(),
                    imageVector = Icons.Outlined.PersonOutline,
                    title = stringResource(id = R.string.label_no_provisioners_available)
                )

                false -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .consumeWindowInsets(paddingValues = paddingValues),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    // Removed in favor of padding in SwipeToDismissProvisioner so that hiding an item will not leave any gaps
                    //verticalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    itemsIndexed(
                        items = provisioners,
                        key = { _, item -> item.id }
                    ) { index, item ->
                        var visibility by remember { mutableStateOf(true) }
                        if (index == 0) {
                            SectionTitle(
                                modifier = Modifier.padding(vertical = 8.dp),
                                title = stringResource(id = R.string.label_this_provisioner)
                            )
                        }
                        if (index == 1) {
                            SectionTitle(
                                modifier = Modifier.padding(bottom = 8.dp),
                                title = stringResource(id = R.string.label_other_provisioner)
                            )
                        }
                        AnimatedVisibility(visibility) {
                            SwipeToDismissProvisioner(
                                index = index,
                                provisioner = item,
                                scope = scope,
                                context = context,
                                snackbarHostState = snackbarHostState,
                                isSelected = selectedUuid == item.uuid.toString() && highlightSelectedItem,
                                navigateToProvisioner = {
                                    selectedUuid = it.toString()
                                    navigateToProvisioner(it)
                                },
                                onSwiped = {
                                    visibility = false
                                    onSwiped(it)
                                },
                                onUndoClicked = {
                                    visibility = true
                                    onUndoClicked(it)
                                },
                                remove = remove,
                                isOnlyProvisioner = provisioners.size == 1,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@Composable
private fun SwipeToDismissProvisioner(
    scope: CoroutineScope,
    context: Context,
    snackbarHostState: SnackbarHostState,
    provisioner: ProvisionerData,
    navigateToProvisioner: (Uuid) -> Unit,
    onSwiped: (ProvisionerData) -> Unit,
    onUndoClicked: (ProvisionerData) -> Unit,
    remove: (ProvisionerData) -> Unit,
    isSelected: Boolean = false,
    isOnlyProvisioner: Boolean,
    index: Int,
) {
    val dismissState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        // Added instead of using Arrangement.spacedBy to avoid leaving gaps when an item is swiped away.
        modifier = Modifier.padding(bottom = 8.dp),
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.Settled,
                    SwipeToDismissBoxValue.StartToEnd,
                    SwipeToDismissBoxValue.EndToStart,
                        -> if (isOnlyProvisioner) Color.Gray else Color.Red

                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = color, shape = CardDefaults.elevatedShape)
            )
        },
        onDismiss = {
            snackbarHostState.currentSnackbarData?.dismiss()
            if (isOnlyProvisioner) {
                // The following functions are invoked in their own coroutine to ensure
                // that they are executed sequentially
                scope.launch {
                    dismissState.reset()
                }
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.error_cannot_delete_last_provisioner,
                            provisioner.name
                        )
                    )
                }
            } else {
                scope.launch {
                    onSwiped(provisioner)
                }
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = context.getString(
                            R.string.label_provisioner_deleted,
                            provisioner.name
                        ),
                        actionLabel = context.getString(R.string.action_undo),
                        withDismissAction = true,
                        duration = SnackbarDuration.Short
                    )

                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            dismissState.reset()
                            onUndoClicked(provisioner)
                        }

                        SnackbarResult.Dismissed -> remove(provisioner)
                    }
                }
            }
        },
        content = {
            ElevatedCardItem(
                colors = isSelected.selectedColor(),
                onClick = { navigateToProvisioner(provisioner.uuid) },
                imageVector = index.toImageVector(),
                title = provisioner.name,
                subtitle = provisioner.address?.let {
                    it.address.toHexString(
                        format = HexFormat {
                            number.prefix = "Address: 0x"
                            upperCase = true
                        }
                    )
                } ?: context.getString(R.string.label_unassigned),
            )
        }
    )
}

@Composable
private fun Boolean.selectedColor() = when (this) {
    true -> CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    )

    else -> CardDefaults.outlinedCardColors()
}

@Composable
private fun Int.toImageVector() = when (this == 0) {
    true -> Icons.Filled.PersonPin
    false -> Icons.Outlined.PersonOutline
}