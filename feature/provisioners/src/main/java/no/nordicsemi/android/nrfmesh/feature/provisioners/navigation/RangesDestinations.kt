@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.net.Uri
import androidx.navigation.NavBackStackEntry
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import java.util.UUID


object RangesDestination : MeshNavigationDestination {
    const val rangesUuidArg = "rangesUuidArg"
    override val route: String = "provisioner_route/{$rangesUuidArg}"
    override val destination: String = "ranges_destination"

    /**
     * Creates destination route for a provisioner UUID.
     */
    fun createNavigationRoute(provisionerUuid: UUID): String =
        "ranges_route/${Uri.encode(provisionerUuid.toString())}"

    /**
     * Returns the provisioner uuid index from a [NavBackStackEntry] after a topic destination
     * navigation call.
     */
    fun fromNavArgs(entry: NavBackStackEntry): String {
        val encodedId = entry.arguments?.getString(rangesUuidArg)!!
        return Uri.decode(encodedId)
    }
}