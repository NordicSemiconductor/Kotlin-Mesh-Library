package no.nordicsemi.android.feature.config.networkkeys.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import java.util.UUID

object ConfigNetworkKeyDestination : MeshNavigationDestination {
    const val arg = "arg"
    override val route: String = "config_net_key_route/{$arg}"
    override val destination: String = "config_net_key_destination"

    /**
     * Creates destination route for a network key index.
     *
     * @param uuid UUID of the node to which the key is added
     */
    fun createNavigationRoute(uuid: UUID): String =
        "config_net_key_route/${Uri.encode(uuid.toString())}"

    /**
     * Returns the topicId from a [NavBackStackEntry] after a topic destination navigation call
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(arg)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.configNetworkKeyGraph(onBackPressed: () -> Unit) {
    composable(route = ConfigNetworkKeyDestination.route) {
        // ConfigNetKeyRoute(onBackPressed = onBackPressed)
    }
}

