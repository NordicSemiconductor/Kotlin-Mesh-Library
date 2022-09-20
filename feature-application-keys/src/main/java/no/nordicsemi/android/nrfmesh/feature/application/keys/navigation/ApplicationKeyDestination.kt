@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object ApplicationKeyDestination : MeshNavigationDestination {
    const val appKeyIndexArg = "appKeyIndexArg"
    override val route: String = "application_key_route/{$appKeyIndexArg}"
    override val destination: String = "application_key_destination"

    /**
     * Creates destination route for a application key index.
     */
    fun createNavigationRoute(appKeyIndexArg: KeyIndex): String =
        "application_key_route/${Uri.encode(appKeyIndexArg.toInt().toString())}"

    /**
     * Returns the application key index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(appKeyIndexArg)!!
        return Uri.decode(encodedId)
    }
}

fun NavGraphBuilder.applicationKeyGraph(onBackPressed: () -> Unit) {
    composable(route = ApplicationKeyDestination.route) {
        ApplicationKeyRoute(onBackPressed = onBackPressed)
    }
}