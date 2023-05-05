@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package no.nordicsemi.android.nrfmesh.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.popUpToStartDestination
import no.nordicsemi.android.common.navigation.with
import no.nordicsemi.android.common.theme.view.NordicLargeAppBar
import no.nordicsemi.android.kotlin.mesh.bearer.android.utils.MeshProvisioningService
import no.nordicsemi.android.nrfmesh.destinations.NavigationItem
import no.nordicsemi.android.nrfmesh.destinations.groupsTab
import no.nordicsemi.android.nrfmesh.destinations.navigationItems
import no.nordicsemi.android.nrfmesh.destinations.nodesTab
import no.nordicsemi.android.nrfmesh.destinations.provisioning
import no.nordicsemi.android.nrfmesh.destinations.provisioningDestination
import no.nordicsemi.android.nrfmesh.destinations.proxyFilterTab
import no.nordicsemi.android.nrfmesh.destinations.settingsTab
import no.nordicsemi.android.nrfmesh.destinations.topLevelTabs
import no.nordicsemi.android.nrfmesh.feature.application.keys.destinations.applicationKeys
import no.nordicsemi.android.nrfmesh.feature.groups.destinations.groups
import no.nordicsemi.android.nrfmesh.feature.groups.destinations.groupsDestinations
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeys
import no.nordicsemi.android.nrfmesh.feature.nodes.R
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.nodes
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.nodesDestinations
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.groupRanges
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioner
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioners
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.sceneRanges
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.unicastRanges
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.destination.proxyFilter
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.destination.proxyFilterDestinations
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scene
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scenes
import no.nordicsemi.android.nrfmesh.feature.settings.destinations.settings
import no.nordicsemi.android.nrfmesh.feature.settings.destinations.settingsDestinations
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel

@Composable
fun NetworkRoute(viewModel: NetworkViewModel = hiltViewModel()) {
    if (viewModel.isNetworkLoaded)
        NetworkScreen(viewModel)
}

@Composable
fun NetworkScreen(viewModel: NetworkViewModel) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val currentDestination by viewModel.currentDestination().collectAsStateWithLifecycle()
    var openBottomSheet by rememberSaveable { mutableStateOf(false) }
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        sheetContent = {
            ScannerSheet(
                service = MeshProvisioningService,
                onDeviceFound = {
                    hideScanner(scope, bottomSheetState)
                    viewModel.navigateTo(provisioning, it)
                },
                hideScanner = { hideScanner(scope, bottomSheetState) }
            )
        },
        sheetState = bottomSheetState
    ) {
        if (bottomSheetState.isVisible) {
            BackHandler(enabled = true, onBack = { hideScanner(scope, bottomSheetState) })
        }
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                NordicLargeAppBar(
                    text = currentDestination?.title() ?: "",
                    scrollBehavior = scrollBehavior,
                    backButtonIcon = when (currentDestination) {
                        provisioning -> Icons.Rounded.Close
                        else -> Icons.Rounded.ArrowBack
                    },
                    onNavigationButtonClick = { viewModel.navigateUp() },
                    showBackButton = when (currentDestination) {
                        nodes, groups, proxyFilter, settings -> false
                        else -> true
                    }
                )
            },
            floatingActionButton = {
                if (currentDestination == nodes) {
                    ExtendedFloatingActionButton(onClick = {
                        if (!bottomSheetState.isVisible) {
                            openBottomSheet = true
                            scope.launch {
                                bottomSheetState.show()
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = null)
                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(R.string.action_add_node)
                        )
                    }
                }
            },
            bottomBar = {
                if (currentDestination != provisioning)
                    BottomNavigationBar(
                        destinations = navigationItems,
                        navigator = viewModel.navigator
                    )
            }
        ) {
            NavigationView(
                destinations = listOf(
                    topLevelTabs with ((nodesTab with nodesDestinations) +
                            (groupsTab with groupsDestinations) +
                            (proxyFilterTab with proxyFilterDestinations) +
                            (settingsTab with settingsDestinations)) + provisioningDestination
                ),
                modifier = Modifier.padding(it)
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    destinations: List<NavigationItem>,
    navigator: Navigator
) {
    NavigationBar {
        destinations.forEach { destination ->
            val selected by navigator.isInHierarchy(destination.destinationId)
                .collectAsStateWithLifecycle()
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selected) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = null
                    )
                },
                selected = selected,
                label = { Text(stringResource(destination.iconTextId)) },
                onClick = {
                    // Checking if the tab is not selected here
                    // is a workaround for an issue with how the navigation
                    // restores the previous stack when back button was used.
                    // See: https://issuetracker.google.com/issues/258237571
                    if (!selected) {
                        navigator.navigateTo(destination.destinationId) {
                            popUpToStartDestination { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

internal fun hideScanner(scope: CoroutineScope, bottomSheetState: ModalBottomSheetState) {
    scope.launch { bottomSheetState.hide() }
}

@Composable
fun DestinationId<*, *>.title(): String {
    return when (this) {
        nodes, groups, proxyFilter, settings -> "Network"
        provisioning -> "Provisioner"
        provisioners -> "Provisioners"
        provisioner -> "Edit Provisioner"
        networkKeys -> "Network Keys"
        applicationKeys -> "Application Keys"
        networkKey -> "Edit Key"
        scenes -> "Scenes"
        scene -> "Edit Scene"
        unicastRanges, groupRanges, sceneRanges -> "Edit Ranges"
        else -> ""
    }
}