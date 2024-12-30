package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerViewModel
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.GroupRangesDestination
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.SceneRangesDestination
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.UnicastRangesDestination
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.groupRangesGraph
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.sceneRangesGraph
import no.nordicsemi.android.nrfmesh.feature.ranges.navigation.unicastRangesGraph
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.UUID
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Parcelize
data class ProvisionerRoute(val uuid: UUID) : Parcelable

object ProvisionerDestination : MeshNavigationDestination {
    override val route: String = "provisioner_route/{$ARG}"
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
        val encodedId = entry.arguments?.getString(ARG)!!
        return Uri.decode(encodedId)
    }
}

internal fun NavGraphBuilder.provisionerGraph(
    appState: AppState,
    onNavigateToUnicastRanges: (MeshNavigationDestination, String) -> Unit,
    onNavigateToGroupRanges: (MeshNavigationDestination, String) -> Unit,
    onNavigateToSceneRanges: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
) {
    /*composable(route = ProvisionerDestination.route) {
        val viewModel = hiltViewModel<ProvisionerViewModel>()
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        ProvisionerRoute(
            appState = appState,
            uiState = uiState,
            onNameChanged = viewModel::onNameChanged,
            onAddressChanged = viewModel::onAddressChanged,
            disableConfigurationCapabilities = { *//*TODO*//* },
            onTtlChanged = viewModel::onTtlChanged,
            isValidAddress = viewModel::isValidAddress,
            navigateToUnicastRanges = {
                onNavigateToUnicastRanges(
                    UnicastRangesDestination,
                    UnicastRangesDestination.createNavigationRoute(
                        provisionerUuid = it
                    )
                )
            },
            navigateToGroupRanges = {
                onNavigateToGroupRanges(
                    GroupRangesDestination,
                    GroupRangesDestination.createNavigationRoute(
                        provisionerUuid = it
                    )
                )
            },
            navigateToSceneRanges = {
                onNavigateToSceneRanges(
                    SceneRangesDestination,
                    SceneRangesDestination.createNavigationRoute(
                        provisionerUuid = it
                    )
                )
            },
            onBackPressed = onBackPressed
        )
    }*/
    unicastRangesGraph(
        appState = appState,
        onBackPressed = onBackPressed
    )
    groupRangesGraph(
        appState = appState,
        onBackPressed = onBackPressed
    )
    sceneRangesGraph(
        appState = appState,
        onBackPressed = onBackPressed
    )
}

@Composable
fun ProvisionerScreenRoute(
    appState: AppState,
    provisioner: Provisioner,
    otherProvisioners : List<Provisioner>,
    onNavigateToUnicastRanges: (MeshNavigationDestination, String) -> Unit,
    onNavigateToGroupRanges: (MeshNavigationDestination, String) -> Unit,
    onNavigateToSceneRanges: (MeshNavigationDestination, String) -> Unit,
    onBackPressed: () -> Unit
){
    val viewModel = hiltViewModel<ProvisionerViewModel>()
    ProvisionerRoute(
        appState = appState,
        provisioner = provisioner,
        otherProvisioners = otherProvisioners,
        onNameChanged = viewModel::onNameChanged,
        onAddressChanged = viewModel::onAddressChanged,
        disableConfigurationCapabilities = { /*TODO*/ },
        onTtlChanged = viewModel::onTtlChanged,
        isValidAddress = viewModel::isValidAddress,
        navigateToUnicastRanges = {
            onNavigateToUnicastRanges(
                UnicastRangesDestination,
                UnicastRangesDestination.createNavigationRoute(
                    provisionerUuid = it
                )
            )
        },
        navigateToGroupRanges = {
            onNavigateToGroupRanges(
                GroupRangesDestination,
                GroupRangesDestination.createNavigationRoute(
                    provisionerUuid = it
                )
            )
        },
        navigateToSceneRanges = {
            onNavigateToSceneRanges(
                SceneRangesDestination,
                SceneRangesDestination.createNavigationRoute(
                    provisionerUuid = it
                )
            )
        },
        onBackPressed = onBackPressed
    )
}