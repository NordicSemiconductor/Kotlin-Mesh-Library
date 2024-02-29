@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.ui.network

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Hub
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import no.nordicsemi.android.common.navigation.DestinationId
import no.nordicsemi.android.common.navigation.NavigationView
import no.nordicsemi.android.common.navigation.Navigator
import no.nordicsemi.android.common.navigation.popUpToStartDestination
import no.nordicsemi.android.common.navigation.with
import no.nordicsemi.android.common.ui.view.NordicLargeAppBar
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.destinations.NavigationItem
import no.nordicsemi.android.nrfmesh.destinations.groupsTab
import no.nordicsemi.android.nrfmesh.destinations.navigationItems
import no.nordicsemi.android.nrfmesh.destinations.netKeySelector
import no.nordicsemi.android.nrfmesh.destinations.netKeySelectorDestination
import no.nordicsemi.android.nrfmesh.destinations.nodesTab
import no.nordicsemi.android.nrfmesh.destinations.provisioning
import no.nordicsemi.android.nrfmesh.destinations.provisioningDestination
import no.nordicsemi.android.nrfmesh.destinations.proxyFilterTab
import no.nordicsemi.android.nrfmesh.destinations.settingsTab
import no.nordicsemi.android.nrfmesh.destinations.topLevelTabs
import no.nordicsemi.android.nrfmesh.feature.application.keys.destinations.applicationKeys
import no.nordicsemi.android.nrfmesh.feature.export.destination.export
import no.nordicsemi.android.nrfmesh.feature.export.destination.exportDestinations
import no.nordicsemi.android.nrfmesh.feature.groups.destinations.groups
import no.nordicsemi.android.nrfmesh.feature.groups.destinations.groupsDestinations
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.destinations.networkKeys
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.netKeys
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.node
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.nodes
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.nodesDestinations
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.groupRanges
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioner
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.provisioners
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.sceneRanges
import no.nordicsemi.android.nrfmesh.feature.provisioners.destinations.unicastRanges
import no.nordicsemi.android.nrfmesh.feature.proxy.destination.proxy
import no.nordicsemi.android.nrfmesh.feature.proxy.destination.proxyDestinations
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scene
import no.nordicsemi.android.nrfmesh.feature.scenes.destination.scenes
import no.nordicsemi.android.nrfmesh.feature.settings.SettingsDropDown
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
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val currentDestination by viewModel.currentDestination().collectAsStateWithLifecycle()
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.importNetwork(uri, context.contentResolver)
        }
    }

    var isOptionsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val density = LocalDensity.current
    val enterTransition: EnterTransition = slideInVertically {
        // Slide in from 40 dp from the top.
        with(density) { -40.dp.roundToPx() }
    } + expandVertically(
        // Expand from the top.
        expandFrom = Alignment.Top
    ) + fadeIn(
        // Fade in with the initial alpha of 0.3f.
        initialAlpha = 0.3f
    )
    val exitTransition: ExitTransition = slideOutVertically() + shrinkVertically() + fadeOut()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NordicLargeAppBar(
                title = {
                    Text(text = currentDestination?.title() ?: "")
                },
                scrollBehavior = scrollBehavior,
                backButtonIcon = when (currentDestination) {
                    provisioning -> Icons.Rounded.Close
                    else -> Icons.AutoMirrored.Rounded.ArrowBack
                },
                onNavigationButtonClick = viewModel::navigateUp,
                showBackButton = when (currentDestination) {
                    nodes, groups, proxy, settings -> false
                    else -> true
                },
                actions = {
                    if (currentDestination == settings) {
                        IconButton(onClick = { isOptionsMenuExpanded = !isOptionsMenuExpanded }) {
                            Icon(imageVector = Icons.Outlined.MoreVert, contentDescription = null)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (currentDestination == nodes || currentDestination == groups) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                    onClick = { viewModel.navigateTo(provisioning) }
                ) {
                    Icon(
                        imageVector = when (currentDestination) {
                            proxy -> Icons.Rounded.Hub
                            else -> Icons.Rounded.Add
                        }, contentDescription = null
                    )
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = when (currentDestination) {
                            nodes -> stringResource(R.string.action_add_node)
                            groups -> stringResource(R.string.action_add_group)
                            proxy -> stringResource(R.string.action_connect)
                            else -> ""
                        }
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = currentDestination?.shouldShowTopBottomBars() ?: false,
                enter = enterTransition,
                exit = exitTransition
            ) {
                BottomNavigationBar(
                    destinations = navigationItems,
                    navigator = viewModel.navigator
                )
            }
        }
    ) { paddingValues ->
        NavigationView(
            destinations = listOf(
                topLevelTabs with ((nodesTab with nodesDestinations) +
                        (groupsTab with groupsDestinations) +
                        (proxyFilterTab with proxyDestinations) +
                        (settingsTab with settingsDestinations)) +
                        provisioningDestination +
                        netKeySelectorDestination +
                        exportDestinations
            ),
            modifier = Modifier.padding(paddingValues)
        )
        SettingsDropDown(
            navigate = {
                isOptionsMenuExpanded = !isOptionsMenuExpanded
                viewModel.launchExport()
            },
            isOptionsMenuExpanded = isOptionsMenuExpanded,
            onDismiss = { isOptionsMenuExpanded = !isOptionsMenuExpanded },
            importNetwork = {
                isOptionsMenuExpanded = !isOptionsMenuExpanded
                fileLauncher.launch("application/json")
            },
            resetNetwork = {
                viewModel.resetNetwork()
            }
        )
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

@Composable
fun DestinationId<*, *>.title(): String {
    return when (this) {
        nodes, groups, proxy, settings -> "Network"
        node -> "Node"
        provisioning -> "Provision Device"
        netKeySelector -> "Select Network Key"
        provisioners -> "Provisioners"
        provisioner -> "Edit Provisioner"
        networkKeys -> "Network Keys"
        netKeys -> "Network Keys 1"
        applicationKeys -> "Application Keys"
        networkKey -> "Edit Key"
        scenes -> "Scenes"
        scene -> "Edit Scene"
        unicastRanges, groupRanges, sceneRanges -> "Edit Ranges"
        export -> "Export"
        else -> ""
    }
}

private fun DestinationId<*, *>.shouldShowTopBottomBars() = when (this) {
    provisioning -> false
    else -> true
}
