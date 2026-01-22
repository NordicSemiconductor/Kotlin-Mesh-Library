package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.GroupsKey
import no.nordicsemi.android.nrfmesh.core.navigation.MESH_TOP_LEVEL_NAV_ITEMS
import no.nordicsemi.android.nrfmesh.core.navigation.NavigationState
import no.nordicsemi.android.nrfmesh.core.navigation.NodeKey
import no.nordicsemi.android.nrfmesh.core.navigation.NodesKey
import no.nordicsemi.android.nrfmesh.core.navigation.ProxyKey
import no.nordicsemi.android.nrfmesh.core.navigation.SettingsKey
import no.nordicsemi.android.nrfmesh.core.navigation.rememberNavigationState
import no.nordicsemi.android.nrfmesh.feature.groups.group.navigation.GroupKey
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberMeshAppState(
    snackbarHostState: SnackbarHostState,
): MeshAppState {
    val navigationState = rememberNavigationState(
        startKey = NodesKey,
        topLevelKeys = MESH_TOP_LEVEL_NAV_ITEMS.keys
    )
    return remember(navigationState) {
        MeshAppState(
            navigationState = navigationState,
            snackbarHostState = snackbarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Stable
class MeshAppState(
    navigationState: NavigationState,
    snackbarHostState: SnackbarHostState,
) : AppState(
    navigationState = navigationState,
    snackbarHostState = snackbarHostState
) {
    val showBackButton: Boolean
        get() = !navigationState.topLevelKeys.contains(navigationState.currentKey)

    val title: String
        get() = when (navigationState.currentKey) {
            is NodesKey -> "Nodes"
            is NodeKey -> "Node"
            is GroupsKey -> "Groups"
            is GroupKey -> "Group"
            is ProxyKey -> "Proxy"
            is SettingsKey -> "Settings"
            is ProvisioningKey -> "Add Node"
            else -> "Unknown"
        }
}