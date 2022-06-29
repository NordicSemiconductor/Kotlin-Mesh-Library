package no.nordicsemi.android.nrfmesh

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import no.nordicsemi.android.nrfmesh.core.ui.MeshDropDown
import no.nordicsemi.android.nrfmesh.core.ui.MeshLargeTopAppBar
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.groupsGraph
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.nodesGraph
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.navigation.proxyFilterGraph
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.settingsGraph
import no.nordicsemi.android.nrfmesh.navigation.MeshTopLevelNavigation
import no.nordicsemi.android.nrfmesh.navigation.TOP_LEVEL_DESTINATIONS
import no.nordicsemi.android.nrfmesh.navigation.TopLevelDestination
import no.nordicsemi.android.nrfmesh.viewmodel.NetworkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen(
    viewModel: NetworkViewModel = hiltViewModel(),
    onCancelled: () -> Unit = {}
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val meshTopLevelNavigation = remember(navController) { MeshTopLevelNavigation(navController) }
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.importNetwork(uri = uri, contentResolver = context.contentResolver) }
    }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        decayAnimationSpec,
        rememberTopAppBarScrollState()
    )
    var isOptionsMenuExpanded by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        topBar = {
            MeshLargeTopAppBar(
                title = stringResource(R.string.label_network),
                scrollBehavior = scrollBehavior,
                showOverflowMenu = currentDestination?.route == SettingsDestination.route,
                onOverflowMenuClicked = {
                    isOptionsMenuExpanded = !isOptionsMenuExpanded
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                onNavigateToTopLevelDestination = meshTopLevelNavigation::navigateTo,
                currentDestination = currentDestination
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = SettingsDestination.route
            ) {
                nodesGraph()
                groupsGraph()
                proxyFilterGraph()
                settingsGraph()
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.TopEnd)
    ) {
        MeshDropDown(
            expanded = isOptionsMenuExpanded,
            onDismiss = { isOptionsMenuExpanded = !isOptionsMenuExpanded }) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Upload, contentDescription = null)
                },
                text = {
                    Text(
                        text = stringResource(R.string.label_import),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                onClick = {
                    isOptionsMenuExpanded = !isOptionsMenuExpanded
                    fileLauncher.launch("application/json")
                }
            )
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Download, contentDescription = null)
                },
                text = {
                    Text(
                        text = stringResource(R.string.label_export),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                onClick = { /*TODO*/ }
            )
            MenuDefaults.Divider()
            DropdownMenuItem(
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.LockReset, contentDescription = null)
                },
                text = {
                    Text(
                        text = stringResource(R.string.label_reset),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                onClick = { /*TODO*/ }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    onNavigateToTopLevelDestination: (TopLevelDestination) -> Unit,
    currentDestination: NavDestination?
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        NavigationBar(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                )
            ),
            tonalElevation = 0.dp
        ) {
            TOP_LEVEL_DESTINATIONS.forEach { destination ->
                val selected =
                    currentDestination?.hierarchy?.any { it.route == destination.route } == true
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
}
