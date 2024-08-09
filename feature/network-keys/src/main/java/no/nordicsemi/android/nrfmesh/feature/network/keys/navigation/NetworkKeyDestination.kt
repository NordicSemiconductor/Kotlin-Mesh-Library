package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

object NetworkKeyDestination : MeshNavigationDestination {
    const val netKeyIndexArg = "netKeyIndexArg"
    override val route: String = "network_key_route/{$netKeyIndexArg}"
    override val destination: String = "network_key_destination"

    /**
     * Creates destination route for a network key index.
     */
    fun createNavigationRoute(netKeyIndexArg: KeyIndex): String =
        "network_key_route/${Uri.encode(netKeyIndexArg.toInt().toString())}"

    /**
     * Returns the topicId from a [NavBackStackEntry] after a topic destination navigation call
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(netKeyIndexArg)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.networkKeyGraph(onBackPressed: () -> Unit) {
    composable(route = NetworkKeyDestination.route) {
        // NetworkKeyRoute(onBackPressed = onBackPressed)
    }
}

