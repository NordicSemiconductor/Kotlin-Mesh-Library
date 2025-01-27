package no.nordicsemi.android.nrfmesh.ui.network

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.common.ui.view.NordicAppBar
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.navigation.MeshNavHost
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelDestination
import no.nordicsemi.android.nrfmesh.navigation.rememberMeshAppState

@Composable
fun NetworkRoute(windowSizeClass: WindowSizeClass) {
    NetworkScreen(windowSizeClass = windowSizeClass)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(windowSizeClass: WindowSizeClass) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appState = rememberMeshAppState(
        navController = navController,
        scope = scope,
        snackbarHostState = snackbarHostState,
        windowSizeClass = windowSizeClass
    )
    val currentDestination = appState.currentDestination
    var menuExpanded by remember { mutableStateOf(false) }
    println("Current destination: ${navController.currentDestination}")
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
            contentWindowInsets = WindowInsets.displayCutout.union(WindowInsets.navigationBars),
            topBar = {
                NordicAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                id = appState.currentMeshTopLevelDestination
                                    ?.titleTextId
                                    ?: R.string.label_empty
                            )
                        )
                    },
                    backButtonIcon = appState.navigationIcon,
                    showBackButton = appState.onNavigationIconClick != null,
                    onNavigationButtonClick = appState.onNavigationIconClick,
                    actions = {}
                )
            },
            floatingActionButton = {
                appState.floatingActionButton.forEach {
                    ExtendedFloatingActionButton(
                        modifier = Modifier.defaultMinSize(minWidth = 150.dp),
                        text = { Text(text = it.text) },
                        icon = { Icon(imageVector = it.icon, contentDescription = null) },
                        onClick = it.onClick,
                        expanded = true
                    )
                }
            }
        ) { paddingValues ->
            MeshNavHost(
                appState = appState,
                onNavigateToDestination = appState::navigate,
                onBackPressed = appState::onBackPressed,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

private fun NavDestination?.isTopLevelDestinationInHierarchy(
    destination: MeshTopLevelDestination,
) = this?.hierarchy?.any {
    it.route?.contains(destination.name, true) ?: false
} ?: false