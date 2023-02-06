@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package no.nordicsemi.android.nrfmesh.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.common.navigation.*
import no.nordicsemi.android.common.theme.view.NordicLargeAppBar
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.destinations.*
import no.nordicsemi.android.nrfmesh.feature.groups.destinations.groups
import no.nordicsemi.android.nrfmesh.feature.groups.destinations.groupsDestinations
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.nodes
import no.nordicsemi.android.nrfmesh.feature.nodes.destinations.nodesDestinations
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.destination.proxyFilter
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.destination.proxyFilterDestinations
import no.nordicsemi.android.nrfmesh.feature.settings.destinations.settings
import no.nordicsemi.android.nrfmesh.feature.settings.destinations.settingsDestinations
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel


@Composable
fun MeshApp(viewModel: NetworkViewModel = hiltViewModel()) {
    if (viewModel.isNetworkLoaded)
        NetworkScreen(viewModel)
}

@Composable
fun NetworkScreen(viewModel: NetworkViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val currentDestination by viewModel.navigator.currentDestination().collectAsState()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NordicLargeAppBar(
                text = "Network",
                scrollBehavior = scrollBehavior,
                onNavigationButtonClick = {
                    viewModel.navigateUp()
                },
                showBackButton = when (currentDestination) {
                    nodes, groups, proxyFilter, settings -> false
                    else -> true
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                destinations = navigationItems,
                navigator = viewModel.navigator
            )
        }
    ) { padding ->
        NavigationView(
            destinations = listOf(
                topLevelTabs with ((nodesTab with nodesDestinations) +
                        (groupsTab with groupsDestinations) +
                        (proxyFilterTab with proxyFilterDestinations) +
                        (settingsTab with settingsDestinations))
            ),
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun BottomNavigationBar(
    destinations: List<NavigationItem>,
    navigator: Navigator
) {
    NavigationBar(
        modifier = Modifier.windowInsetsPadding(
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
            )
        ),
        tonalElevation = 0.dp
    ) {
        destinations.forEach { destination ->
            val selected by navigator.isInHierarchy(destination.destinationId).collectAsState()
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
                            popUpToStartDestination {
                                saveState = true
                            }
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
    return when(this){
        nodes,groups,proxyFilter,settings -> "Network"
        else -> ""
    }
}