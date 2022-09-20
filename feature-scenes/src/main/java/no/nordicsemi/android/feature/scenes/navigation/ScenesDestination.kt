package no.nordicsemi.android.feature.scenes.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.feature.scenes.ScenesRoute
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber

object ScenesDestination : MeshNavigationDestination {
    override val route: String = "scenes_route"
    override val destination: String = "scenes_destination"
}

fun NavGraphBuilder.scenesGraph(
    onBackPressed: () -> Unit,
    onNavigateToScene: (SceneNumber) -> Unit
) {
    composable(route = ScenesDestination.route) {
        ScenesRoute(
            navigateToScene = onNavigateToScene,
            onBackClicked = onBackPressed
        )
    }
    sceneGraph(onBackPressed = onBackPressed)
}