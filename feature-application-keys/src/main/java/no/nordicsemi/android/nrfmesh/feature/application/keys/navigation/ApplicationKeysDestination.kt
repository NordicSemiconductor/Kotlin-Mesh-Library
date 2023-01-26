@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeysRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object ApplicationKeysDestination : MeshNavigationDestination {
    override val route: String = "application_keys_route"
    override val destination: String = "application_keys_destination"
}

fun NavGraphBuilder.applicationKeysGraph(
    onBackPressed: () -> Unit,
    onNavigateToApplicationKey: (KeyIndex) -> Unit
) {
    composable(route = ApplicationKeysDestination.route) {
        ApplicationKeysRoute(
            navigateToApplicationKey = onNavigateToApplicationKey,
            onBackClicked = onBackPressed
        )
    }
    applicationKeyGraph(onBackPressed = onBackPressed)
}