package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.nrfmesh.core.ui.PlaceHolder
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.ivindex.navigation.IvIndexContent
import no.nordicsemi.android.nrfmesh.feature.ivindex.navigation.IvIndexScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersScreenRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesScreenRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun SettingsDetailsPane(
    content: Any?,
    snackbarHostState: SnackbarHostState,
    highlightSelectedItem: Boolean,
    navigateToProvisioner: (Uuid) -> Unit,
    navigateToNetworkKey: (KeyIndex) -> Unit,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    navigateToScene: (SceneNumber) -> Unit,
    navigateUp: () -> Unit,
) {
    when (content) {
        is ProvisionersContent, is ProvisionerContent -> ProvisionersScreenRoute(
            snackbarHostState = snackbarHostState,
            highlightSelectedItem = highlightSelectedItem,
            onProvisionerClicked = navigateToProvisioner,
            navigateToProvisioner = navigateToProvisioner,
            navigateUp = navigateUp
        )

        is NetworkKeysContent, is NetworkKeyContent -> NetworkKeysScreenRoute(
            snackbarHostState = snackbarHostState,
            highlightSelectedItem = highlightSelectedItem,
            onNetworkKeyClicked = navigateToNetworkKey,
            navigateToKey = navigateToNetworkKey,
            navigateUp = navigateUp
        )

        is ApplicationKeysContent, is ApplicationKeyContent -> ApplicationKeysScreenRoute(
            snackbarHostState = snackbarHostState,
            highlightSelectedItem = highlightSelectedItem,
            onApplicationKeyClicked = navigateToApplicationKey,
            navigateToKey = navigateToApplicationKey,
            navigateUp = navigateUp
        )

        is ScenesContent, is SceneContent -> ScenesScreenRoute(
            snackbarHostState = snackbarHostState,
            highlightSelectedItem = highlightSelectedItem,
            onSceneClicked = navigateToScene,
            navigateToScene = navigateToScene,
            navigateUp = navigateUp
        )

        is IvIndexContent -> IvIndexScreenRoute()

        else -> PlaceHolder(
            modifier = Modifier.fillMaxSize(),
            imageVector = Icons.Outlined.Settings,
            text = stringResource(R.string.label_select_settings_item_rationale)
        )
    }
}