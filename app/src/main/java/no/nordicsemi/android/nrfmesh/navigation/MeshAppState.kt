package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import no.nordicsemi.android.feature.config.networkkeys.ConfigNetKeysKey
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.NavigationState
import no.nordicsemi.android.nrfmesh.core.navigation.NodeKey
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.android.nrfmesh.feature.application.keys.key.navigation.ApplicationKeyContentKey
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContentKey
import no.nordicsemi.android.nrfmesh.feature.config.applicationkeys.ConfigAppKeysKey
import no.nordicsemi.android.nrfmesh.feature.groups.group.navigation.GroupKey
import no.nordicsemi.android.nrfmesh.feature.ivindex.navigation.IvIndexContentKey
import no.nordicsemi.android.nrfmesh.feature.model.navigation.ModelKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.key.navigation.NetworkKeyContentKey
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContentKey
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersContentKey
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.navigation.ProvisionerContentKey
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesContentKey
import no.nordicsemi.android.nrfmesh.feature.scenes.scene.navigation.SceneContentKey
import no.nordicsemi.android.nrfmesh.feature.nodes.node.element.navigation.ElementKey
import no.nordicsemi.android.nrfmesh.feature.provisioning.navigation.ProvisioningKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberMeshAppState(
    snackbarHostState: SnackbarHostState,
    navigationState: NavigationState,
): MeshAppState = remember(navigationState) {
    MeshAppState(
        navigationState = navigationState,
        snackbarHostState = snackbarHostState
    )
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
        @Composable get() = when (navigationState.currentKey) {
            is ProvisionersContentKey,
            is NetworkKeysContentKey,
            is ApplicationKeysContentKey,
            is ScenesContentKey,
                -> isCompactWidth

            is ProvisionerContentKey,
            is NetworkKeyContentKey,
            is ApplicationKeyContentKey,
            is SceneContentKey,
            is IvIndexContentKey,
            is GroupKey,
            is NodeKey,
            is ConfigNetKeysKey,
            is ConfigAppKeysKey,
            is ElementKey,
            is ModelKey,
            is ProvisioningKey,
                -> true

            else -> false
        }

    val isCompactWidth: Boolean
        @Composable
        get() = isCompactWidth()
}