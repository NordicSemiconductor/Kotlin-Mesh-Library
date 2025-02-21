package no.nordicsemi.android.nrfmesh.ui.network

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.ui.MeshAlertDialog
import no.nordicsemi.android.nrfmesh.feature.export.navigation.ExportScreenRoute
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.navigateToGroup
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.navigateToProvisioning
import no.nordicsemi.android.nrfmesh.navigation.MeshAppState
import no.nordicsemi.android.nrfmesh.navigation.MeshNavHost
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination
import no.nordicsemi.android.nrfmesh.navigation.rememberMeshAppState
import no.nordicsemi.kotlin.mesh.core.model.Group

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkRoute(
    windowSizeClass: WindowSizeClass,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
    resetNetwork: () -> Unit,
    onAddGroupClicked: () -> Group,
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberMeshAppState(
        navController = navController,
        snackbarHostState = snackbarHostState,
        windowSizeClass = windowSizeClass
    )
    val currentDestination = appState.currentDestination
    val exportSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var menuExpanded by remember { mutableStateOf(false) }
    var showExportBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showResetNetworkDialog by rememberSaveable { mutableStateOf(false) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            appState.meshTopLevelDestinations.forEach { destination ->
                val selected = currentDestination.isTopLevelDestinationInHierarchy(destination)
                item(
                    selected = selected,
                    onClick = { appState.navigateToTopLevelDestination(destination) },
                    icon = {
                        Icon(
                            imageVector = when (selected) {
                                true -> destination.selectedIcon
                                false -> destination.unselectedIcon
                            },
                            contentDescription = null
                        )
                    },
                    label = { Text(stringResource(destination.iconTextId)) }
                )
            }
        }
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            // contentWindowInsets = WindowInsets.displayCutout.union(WindowInsets.navigationBars),
            topBar = {
                NordicAppBar(
                    title = { Text(text = appState.title) },
                    backButtonIcon = Icons.AutoMirrored.Outlined.ArrowBack,
                    showBackButton = appState.showBackButton,
                    onNavigationButtonClick = appState::onBackPressed,
                    actions = {
                        DisplayDropdown(
                            appState = appState,
                            menuExpanded = menuExpanded,
                            onExpandPressed = { menuExpanded = true },
                            onDismissRequest = { menuExpanded = false },
                            importNetwork = { uri, contentResolver ->
                                appState.clearBackStack()
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
                appState.currentMeshTopLevelDestination?.let { dst ->
                    when (dst) {
                        MeshTopLevelDestination.NODES -> {
                            ExtendedFloatingActionButton(
                                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                                text = { Text(text = stringResource(R.string.label_add_node)) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    navController.navigateToProvisioning(navOptions = navOptions { })
                                },
                                expanded = true
                            )
                        }

                        MeshTopLevelDestination.GROUPS -> {
                            ExtendedFloatingActionButton(
                                modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                                text = { Text(text = stringResource(R.string.label_add_group)) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Add,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    runCatching {
                                        onAddGroupClicked().also {
                                            navController.navigateToGroup(address = it.address)
                                        }
                                    }.onFailure {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = it.message
                                                    ?: context.getString(R.string.label_failed_to_add_group),
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    }
                                },
                                expanded = true
                            )
                        }

                        else -> {}
                    }
                }
            }
        ) { paddingValues ->
            MeshNavHost(
                appState = appState,
                onBackPressed = appState::onBackPressed,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )

            if (showResetNetworkDialog) {
                MeshAlertDialog(
                    icon = Icons.Outlined.RestartAlt,
                    iconColor = Color.Red,
                    title = stringResource(R.string.label_reset_network),
                    text = stringResource(R.string.label_reset_network_rationale),
                    onConfirmClick = {
                        scope.launch {
                            appState.clearBackStack()
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
                        ExportScreenRoute(
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
                                    snackbarHostState.showSnackbar(
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
    appState.currentMeshTopLevelDestination?.takeIf {
        it == MeshTopLevelDestination.SETTINGS
    }?.let {
        Box(modifier = Modifier.padding(all = 16.dp)) {
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

private fun NavDestination?.isTopLevelDestinationInHierarchy(
    destination: MeshTopLevelDestination,
) = this?.hierarchy?.any {
    it.route?.contains(destination.name, true) ?: false
} ?: false