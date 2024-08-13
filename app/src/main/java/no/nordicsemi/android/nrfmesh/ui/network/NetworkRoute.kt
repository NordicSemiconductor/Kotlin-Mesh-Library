@file:OptIn(ExperimentalMaterial3Api::class)

package no.nordicsemi.android.nrfmesh.ui.network

import android.util.Log
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import no.nordicsemi.android.common.ui.view.NordicLargeAppBar
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.navigation.MeshNavHost
import no.nordicsemi.android.nrfmesh.core.navigation.TopLevelDestination
import no.nordicsemi.android.nrfmesh.core.ui.ActionsMenu
import no.nordicsemi.android.nrfmesh.navigation.MeshAppState
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel

@Composable
fun NetworkRoute(
    appState: MeshAppState,
    viewModel: NetworkViewModel = hiltViewModel()
) {
    if (viewModel.isNetworkLoaded)
        NetworkScreen(appState = appState, viewModel = viewModel)
}

@Composable
fun NetworkScreen(appState: MeshAppState, viewModel: NetworkViewModel) {
    val currentDestination by appState.navController.currentBackStackEntryAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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
    var menuExpanded by remember { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            NordicLargeAppBar(
                title = {
                    Text(text = appState.title)
                },
                scrollBehavior = scrollBehavior,
                backButtonIcon = appState.navigationIcon,
                showBackButton = appState.onNavigationIconClick != null,
                onNavigationButtonClick = appState.onNavigationIconClick,
                actions = {
                    val items = appState.actions
                    if (items.isNotEmpty()) {
                        ActionsMenu(
                            items = items,
                            isOpen = menuExpanded,
                            onToggleOverflow = {
                                menuExpanded = !menuExpanded
                            },
                            maxVisibleItems = 3
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (appState.showTopAppBar) {
                ExtendedFloatingActionButton(
                    modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                    onClick = {

                    }
                ) {
/*                    Icon(
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
                    )*/
                }
            }
        },
        bottomBar = {
            Log.d("AAA", "${appState.currentScreen?.route}")
            AnimatedVisibility(
                visible = appState.showBottomBar,
                enter = enterTransition,
                exit = exitTransition
            ) {
                BottomNavigationBar(
                    destinations = appState.topLevelDestinations,
                    onNavigateToTopLevelDestination = appState::navigate,
                    currentDestination = appState.currentDestination
                )
            }
        }
    ) { paddingValues ->
        /*SettingsDropDown(
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
                isOptionsMenuExpanded = !isOptionsMenuExpanded
                viewModel.resetNetwork()
            }
        )*/
        MeshNavHost(
            appState = appState,
            navController = appState.navController,
            onNavigateToDestination = appState::navigate,
            onBackPressed = appState::onBackPressed,
            startDestination = NodesDestination.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
            // .safeContentPadding()
        )
    }
}


@Composable
fun BottomNavigationBar(
    destinations: List<TopLevelDestination>,
    onNavigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
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
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToTopLevelDestination(destination) },
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
                label = { Text(stringResource(destination.iconTextId)) }
            )
        }
    }
}
