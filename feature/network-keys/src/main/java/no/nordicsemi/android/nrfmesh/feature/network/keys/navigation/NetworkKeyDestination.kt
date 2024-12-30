package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex

@Parcelize
data class NetworkKeyRoute(val keyIndex: KeyIndex) : Parcelable

object NetworkKeyDestination : MeshNavigationDestination {
    override val route: String = "network_key_route/{$ARG}"
    override val destination: String = "network_key_destination"

    /**
     * Creates destination route for a network key index.
     */
    fun createNavigationRoute(netKeyIndexArg: KeyIndex): String =
        "network_key_route/${Uri.encode(netKeyIndexArg.toInt().toString())}"
}