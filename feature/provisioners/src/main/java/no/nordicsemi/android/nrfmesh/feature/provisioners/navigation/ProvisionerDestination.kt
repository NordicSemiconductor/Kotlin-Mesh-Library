package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerViewModel
import java.util.UUID

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

internal fun NavGraphBuilder.provisionerGraph(
    onNavigateToUnicastRanges: (MeshNavigationDestination, String) -> Unit,
    onNavigateToGroupRanges: (MeshNavigationDestination, String) -> Unit,
    onNavigateToSceneRanges: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    composable(route = ProvisionerDestination.route) {
        val viewModel = hiltViewModel<ProvisionerViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisionerRoute(
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            disableConfigurationCapabilities = { /*TODO*/ },
            onTtlChanged = viewModel::onTtlChanged,
            isValidAddress = viewModel::isValidAddress,
            navigateToUnicastRanges = {
                onNavigateToUnicastRanges(
                    RangesDestination,
                    RangesDestination.createNavigationRoute(
                        provisionerUuid = it
                    )
                )
            },
            navigateToGroupRanges = {
                onNavigateToGroupRanges(
                    RangesDestination,
                    RangesDestination.createNavigationRoute(
                        provisionerUuid = it
                    )
                )
            },
            navigateToSceneRanges = {
                onNavigateToSceneRanges(
                    RangesDestination,
                    RangesDestination.createNavigationRoute(
                        provisionerUuid = it
                    )
                )
            },
        )
        unicastRangesGraph(onBackPressed)
        groupRangesGraph(onBackPressed)
        sceneRangesGraph(onBackPressed)
    }
}