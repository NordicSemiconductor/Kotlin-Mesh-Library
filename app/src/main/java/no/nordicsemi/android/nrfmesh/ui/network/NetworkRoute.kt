@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.ui.network

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.common.ui.view.NordicLargeAppBar
import no.nordicsemi.android.nrfmesh.core.navigation.TopLevelDestination
import no.nordicsemi.android.nrfmesh.core.ui.ActionsMenu
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesScreen
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningDestination
import no.nordicsemi.android.nrfmesh.navigation.MeshNavHost
import no.nordicsemi.android.nrfmesh.navigation.rememberMeshAppState

@Composable
fun NetworkRoute() {
    NetworkScreen()
}

@Composable
fun NetworkScreen() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberMeshAppState(
        navController = navController,
        scope = scope,
        snackbarHostState = snackbarHostState
    )
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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
    var menuExpanded by remember { mutableStateOf(false) }

    val screen = appState.currentScreen as? NodesScreen
    LaunchedEffect(key1 = screen) {
        screen?.buttons?.onEach { button ->
            when (button) {
                NodesScreen.Actions.ADD_NODE -> appState.navigate(
                    destination = ProvisioningDestination,
                    route = ProvisioningDestination.route
                )
            }
        }?.launchIn(this)
    }
    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            NordicLargeAppBar(
                title = { Text(text = appState.title) },
                scrollBehavior = scrollBehavior,
                backButtonIcon = appState.navigationIcon,
                showBackButton = appState.onNavigationIconClick != null,
                onNavigationButtonClick = appState.onNavigationIconClick,
                actions = {
                    val items = appState.actions
                    if (items.isNotEmpty()) {
                        ActionsMenu(
                            items = appState.actions,
                            isOpen = menuExpanded,
                            onToggleOverflow = { menuExpanded = !menuExpanded },
                            maxVisibleItems = 3
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column {
                appState.floatingActionButton.forEach {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                        text = { Text(text = it.text) },
                        icon = {
                            Icon(
                                imageVector = it.icon,
                                contentDescription = null,
                            )
                        },
                        onClick = it.onClick,
                        expanded = true
                    )
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = appState.showBottomBar,
                enter = enterTransition,
                exit = exitTransition
            ) {
                BottomNavigationBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToTopLevelDestination = {
                        appState.navigate(it, it.route)
                    }
                )
            }
        }
    ) { paddingValues ->
        MeshNavHost(
            appState = appState,
            navController = navController,
            onNavigateToDestination = appState::navigate,
            onBackPressed = appState::onBackPressed,
            startDestination = NodesDestination.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}


@Composable
fun BottomNavigationBar(
    destinations: List<TopLevelDestination>,
    onNavigateToTopLevelDestination: (TopLevelDestination) -> Unit,
) {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    NavigationBar {
        destinations.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onNavigateToTopLevelDestination(destination)
                },
                icon = {
                    Icon(
                        if (selectedItem == index) {
                            destination.selectedIcon
                        } else {
                            destination.unselectedIcon
                        },
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(destination.iconTextId)) }
            )
        }
    }
}
