@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerRoute
import java.util.*

object ProvisionerDestination : MeshNavigationDestination {
    const val provisionerUuidArg = "provisionerUuidArg"
    override val route: String = "provisioner_route/{$provisionerUuidArg}"
    override val destination: String = "provisioner_destination"

    /**
     * Creates destination route for a provisioner UUID.
     */
    fun createNavigationRoute(provisionerUuid: UUID): String =
        "provisioner_route/${Uri.encode(provisionerUuid.toString())}"

    /**
     * Returns the provisioner uuid index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(provisionerUuidArg)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.provisionerGraph(onBackPressed: () -> Unit) {
    composable(route = ProvisionerDestination.route) {
        ProvisionerRoute(onBackPressed = onBackPressed)
    }
}