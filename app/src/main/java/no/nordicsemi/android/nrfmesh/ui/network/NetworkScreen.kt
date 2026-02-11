package no.nordicsemi.android.nrfmesh.ui.network

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.rememberSceneState
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.navigation.GroupsKey
import no.nordicsemi.android.nrfmesh.core.navigation.MESH_TOP_LEVEL_NAV_ITEMS
import no.nordicsemi.android.nrfmesh.core.navigation.Navigator
import no.nordicsemi.android.nrfmesh.core.navigation.NodesKey
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsKey
import no.nordicsemi.android.nrfmesh.core.navigation.toEntries
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedTextField
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportScreen
import no.nordicsemi.android.nrfmesh.feature.groups.group.controls.navigation.groupControlsEntry
import no.nordicsemi.android.nrfmesh.feature.groups.group.navigation.GroupKey
import no.nordicsemi.android.nrfmesh.feature.groups.group.navigation.groupEntry
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsEntry
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesEntry
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningKey
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.provisioningEntry
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.proxyEntry
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsEntry
import no.nordicsemi.android.nrfmesh.navigation.MeshAppState
import no.nordicsemi.android.nrfmesh.viewmodel.MeshNetworkState
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkScreenUiState
import no.nordicsemi.kotlin.mesh.core.exception.GroupAlreadyExists
import no.nordicsemi.kotlin.mesh.core.exception.GroupInUse
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.VirtualAddress
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
internal fun NetworkScreen(
    appState: MeshAppState,
    uiState: NetworkScreenUiState,
    shouldSelectProvisioner: Boolean,
    onProvisionerSelected: (provisioner: Provisioner) -> Unit,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
    resetNetwork: () -> Unit,
    onAddGroupClicked: (Group) -> Unit,
    nextAvailableGroupAddress: () -> GroupAddress,
    isCompactWidth: Boolean = isCompactWidth(),
) {
    when (uiState.networkState) {
        MeshNetworkState.Loading -> {}
        is MeshNetworkState.Success -> {
            val context = LocalContext.current
            val topAppBarTitle by remember(
                key1 = appState.navigationState.currentKey,
                key2 = uiState.networkState.network.createKeysForAppTitles()
            ) {
                derivedStateOf {
                    title(
                        context = context,
                        network = uiState.networkState.network,
                        navigationState = appState.navigationState,
                        isCompactWidth = isCompactWidth
                    )
                }
            }
            NetworkContent(
                appState = appState,
                network = uiState.networkState.network,
                shouldSelectProvisioner = shouldSelectProvisioner,
                onProvisionerSelected = onProvisionerSelected,
                importNetwork = importNetwork,
                resetNetwork = resetNetwork,
                onAddGroupClicked = onAddGroupClicked,
                nextAvailableGroupAddress = nextAvailableGroupAddress,
                topAppBarTitle = topAppBarTitle
            )
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class, ExperimentalUuidApi::class,
    ExperimentalMaterial3AdaptiveApi::class, ExperimentalTime::class
)
@Composable
fun NetworkContent(
    appState: MeshAppState,
    network: MeshNetwork,
    shouldSelectProvisioner: Boolean,
    onProvisionerSelected: (provisioner: Provisioner) -> Unit,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
    resetNetwork: () -> Unit,
    onAddGroupClicked: (Group) -> Unit,
    nextAvailableGroupAddress: () -> GroupAddress,
    topAppBarTitle: String,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val selectProvisionerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    var menuExpanded by remember { mutableStateOf(false) }
    var showExportBottomSheet by rememberSaveable { mutableStateOf(false) }
    val exportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showResetNetworkDialog by rememberSaveable { mutableStateOf(false) }
    var showAddGroupDialog by rememberSaveable { mutableStateOf(false) }
    val navigator = remember { Navigator(appState.navigationState) }
    NavigationSuiteScaffold(
        navigationItemVerticalArrangement = Arrangement.Center,
        navigationSuiteColors = NavigationSuiteDefaults.colors(
            navigationRailContainerColor = ShortNavigationBarDefaults.containerColor,
        ),
        navigationItems = {
            MESH_TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                val selected = navKey == appState.navigationState.currentTopLevelKey
                NavigationSuiteItem(
                    selected = selected,
                    onClick = { if (!selected) navigator.navigate(key = navKey) },
                    icon = {
                        Icon(
                            imageVector = when (selected) {
                                true -> navItem.selectedIcon
                                false -> navItem.unselectedIcon
                            },
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(navItem.iconTextId),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                )
            }
        }
    ) {
        val litDetailStrategy = rememberListDetailSceneStrategy<NavKey>()
        val entryProvider = entryProvider {
            nodesEntry(appState = appState, navigator = navigator)
            provisioningEntry(appState = appState, navigator = navigator)
            groupsEntry(appState = appState, navigator = navigator)
            groupEntry(appState = appState, navigator = navigator)
            groupControlsEntry(appState = appState, navigator = navigator)
            proxyEntry()
            settingsEntry(appState = appState, navigator = navigator)
        }
        val entries = appState.navigationState.toEntries(entryProvider = entryProvider)
        val sceneState = rememberSceneState(entries = entries, sceneStrategy = litDetailStrategy, onBack = {
                navigator.goBack()
            }
        )
        val scene = sceneState.currentScene
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = appState.snackbarHostState) },
            topBar = {
                NordicAppBar(
                    title = { Text(text = topAppBarTitle) },
                    backButtonIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                    showBackButton = appState.showBackButton,
                    onNavigationButtonClick = {
                        // TODO to be clarified as of now uses the backbutton behaviour from tbe NavDisplay's NavigationBackHandler
                        // If `enabled` becomes stale (e.g., it was set to false but a gesture was
                        // dispatched in the same frame), this may result in no entries being popped
                        // due to entries.size being smaller than scene.previousEntries.size
                        // but that's preferable to crashing with an IndexOutOfBoundsException
                        repeat(entries.size - scene.previousEntries.size) { navigator.goBack() }
                    },
                    actions = {
                        DisplayDropdown(
                            appState = appState,
                            menuExpanded = menuExpanded,
                            onExpandPressed = { menuExpanded = true },
                            onDismissRequest = { menuExpanded = false },
                            importNetwork = { uri, contentResolver ->
                                importNetwork(uri, contentResolver)
                            },
                            navigateToExport = {
                                menuExpanded = false
                                showExportBottomSheet = true
                            },
                            resetNetwork = {
                                menuExpanded = false
                                showResetNetworkDialog = true
                            }
                        )
                    }
                )
            },
            floatingActionButton = {
                when (appState.navigationState.currentKey) {
                    is NodesKey -> ExtendedFloatingActionButton(
                        modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                        text = { Text(text = stringResource(R.string.label_add_node)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null
                            )
                        },
                        onClick = { navigator.navigate(key = ProvisioningKey) },
                        expanded = true
                    )

                    is GroupsKey -> ExtendedFloatingActionButton(
                        modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                        text = { Text(text = stringResource(R.string.label_add_group)) },
                        icon = {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = null
                            )
                        },
                        onClick = { showAddGroupDialog = true },
                        expanded = true
                    )

                    else -> {}
                }
            }
        ) { padding ->
            NavDisplay(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues = padding),
                entries = appState.navigationState.toEntries(entryProvider = entryProvider),
                sceneStrategy = litDetailStrategy,
                onBack = navigator::goBack
            )
        }

        if (showAddGroupDialog) {
            var isError by rememberSaveable { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf("") }
            val initialValue by remember {
                mutableStateOf(
                    nextAvailableGroupAddress()
                        .address
                        .toHexString(format = HexFormat.UpperCase)
                )
            }
            var address by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                mutableStateOf(
                    TextFieldValue(
                        text = initialValue,
                        selection = TextRange(initialValue.length)
                    )
                )
            }
            MeshAlertDialog(
                icon = Icons.Outlined.GroupWork,
                title = stringResource(R.string.label_add_group),
                text = stringResource(R.string.label_add_group_rationale),
                onDismissRequest = { showAddGroupDialog = false },
                content = {
                    MeshOutlinedTextField(
                        value = address,
                        onValueChanged = {
                            isError = false
                            address = it
                            if (it.text.isNotEmpty()) {
                                if (GroupAddress.isValid(it.text.toUShort(16))) {
                                    isError = false
                                    appState.snackbarHostState.currentSnackbarData?.dismiss()
                                } else {
                                    isError = true
                                    errorMessage =
                                        context.getString(R.string.label_invalid_group_address)
                                }
                            }
                        },
                        label = { Text(text = stringResource(id = R.string.address)) },
                        supportingText = {
                            if (isError) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters
                        ),
                        regex = Regex("^[0-9A-Fa-f]{0,4}$"),
                        isError = isError,
                    )
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                runCatching {
                                    val group = Group(
                                        address = VirtualAddress(uuid = Uuid.random()),
                                        _name = "New Group"
                                    )
                                    onAddGroupClicked(group)
                                        .also {
                                            showAddGroupDialog = false
                                            navigator.navigate(
                                                key = GroupKey(
                                                    address = group.address.toHexString()
                                                )
                                            )
                                        }
                                }.onFailure {
                                    scope.launch {
                                        appState.snackbarHostState.showSnackbar(
                                            message = when (it) {
                                                is GroupAlreadyExists -> context
                                                    .getString(R.string.label_group_already_exists)

                                                is GroupInUse -> context
                                                    .getString(R.string.label_group_in_use)

                                                else -> it.message ?: context
                                                    .getString(R.string.label_failed_to_add_group)
                                            },
                                            duration = SnackbarDuration.Short
                                        )
                                    }
                                }
                            },
                            content = {
                                Text(text = stringResource(R.string.label_virtual_label))
                            }
                        )
                        Spacer(modifier = Modifier.weight(weight = 1f))
                        TextButton(
                            onClick = { showAddGroupDialog = false },
                            content = { Text(text = stringResource(R.string.label_cancel)) }
                        )
                        TextButton(
                            onClick = {
                                if (address.text.isNotEmpty()) {
                                    if (GroupAddress.isValid(address.text.toUShort(16))) {
                                        isError = false
                                        runCatching {
                                            val group = Group(
                                                address = MeshAddress.create(
                                                    address = address.text.toUShort(radix = 16)
                                                ) as GroupAddress,
                                                _name = "New Group"
                                            )
                                            onAddGroupClicked(group).also {
                                                showAddGroupDialog = false
                                                navigator.navigate(
                                                    key = GroupKey(
                                                        address = group.address.toHexString()
                                                    )
                                                )
                                            }
                                        }.onFailure {
                                            scope.launch {
                                                appState.snackbarHostState.showSnackbar(
                                                    message = it.message
                                                        ?: context.getString(R.string.label_failed_to_add_group),
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        }
                                    } else {
                                        isError = true
                                        errorMessage =
                                            context.getString(R.string.label_invalid_group_address)
                                    }
                                }
                            },
                            content = { Text(text = stringResource(R.string.label_add)) }
                        )
                    }
                }
            )
        }
        if (showResetNetworkDialog) {
            MeshAlertDialog(
                icon = Icons.Outlined.RestartAlt,
                iconColor = Color.Red,
                title = stringResource(R.string.label_reset_network),
                text = stringResource(R.string.label_reset_network_rationale),
                onConfirmClick = {
                    scope.launch {
                        navigator.navigate(key = NodesKey)
                    }.invokeOnCompletion {
                        showResetNetworkDialog = false
                        resetNetwork()
                    }
                },
                onDismissClick = { showResetNetworkDialog = false },
                onDismissRequest = { showResetNetworkDialog = false }
            )
        }
        if (showExportBottomSheet) {
            ModalBottomSheet(
                sheetState = exportSheetState,
                onDismissRequest = { showExportBottomSheet = false },
                content = {
                    ExportScreen(
                        onDismissRequest = {
                            scope.launch { exportSheetState.hide() }
                                .invokeOnCompletion {
                                    if (!exportSheetState.isVisible) {
                                        showExportBottomSheet = false
                                    }
                                }
                        },
                        onExportCompleted = { message ->
                            scope.launch {
                                exportSheetState.hide()
                                appState.snackbarHostState.showSnackbar(
                                    message = message,
                                    duration = SnackbarDuration.Short
                                )
                            }.invokeOnCompletion {
                                if (!exportSheetState.isVisible) {
                                    showExportBottomSheet = false
                                }
                            }
                        }
                    )
                }
            )
        }
        if (shouldSelectProvisioner) {
            ModalBottomSheet(
                properties = ModalBottomSheetProperties(
                    shouldDismissOnBackPress = false,
                    shouldDismissOnClickOutside = false
                ),
                sheetState = selectProvisionerSheetState,
                sheetGesturesEnabled = false,
                onDismissRequest = { },
                content = {
                    SectionTitle(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 8.dp),
                        title = stringResource(R.string.label_select_provisioner_rationale),
                        style = MaterialTheme.typography.titleMedium
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(space = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                    ) {
                        items(items = network.provisioners, key = { it.uuid.toString() }) {
                            ElevatedCardItem(
                                imageVector = Icons.Filled.PersonPin,
                                title = it.name,
                                onClick = {
                                    onProvisionerSelected(it)
                                    scope.launch { exportSheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!exportSheetState.isVisible) {
                                                showExportBottomSheet = false
                                            }
                                        }
                                }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun DisplayDropdown(
    appState: MeshAppState,
    menuExpanded: Boolean,
    onExpandPressed: () -> Unit,
    onDismissRequest: () -> Unit,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
    navigateToExport: () -> Unit,
    resetNetwork: () -> Unit,
) {
    val context = LocalContext.current
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            importNetwork(uri, context.contentResolver)
        }
    }
    appState.navigationState.currentKey.takeIf {
        it is SettingsKey
    }?.let {
        Box(
            modifier = Modifier
                .padding(start = 16.dp)
                .padding(vertical = 16.dp)
        ) {
            IconButton(
                onClick = onExpandPressed,
                content = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = null) }
            )
            DropdownMenu(expanded = menuExpanded, onDismissRequest = onDismissRequest) {
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(imageVector = Icons.Outlined.Download, contentDescription = null)
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = stringResource(R.string.label_import)
                            )
                        }
                    },
                    onClick = {
                        fileLauncher.launch("application/json")
                        onDismissRequest()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(imageVector = Icons.Outlined.Upload, contentDescription = null)
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = stringResource(R.string.label_export)
                            )
                        }
                    },
                    onClick = navigateToExport
                )
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteForever,
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier.padding(start = 16.dp),
                                text = stringResource(R.string.label_reset_network)
                            )
                        }
                    },
                    onClick = resetNetwork
                )
            }
        }
    }
}