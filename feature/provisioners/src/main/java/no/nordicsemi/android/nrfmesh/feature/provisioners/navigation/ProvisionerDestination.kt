package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination
import no.nordicsemi.android.nrfmesh.core.navigation.MeshNavigationDestination.Companion.ARG
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerRoute
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.UUID

@Parcelize
data class ProvisionerRoute(val uuid: UUID) : Parcelable

object ProvisionerDestination : MeshNavigationDestination {
    override val route: String = "provisioner_route/{$ARG}"
    override val destination: String = "provisioner_destination"
}

@Composable
fun ProvisionerScreenRoute(
    appState: AppState,
    provisioner: Provisioner,
    otherProvisioners: List<Provisioner>,
    save: () -> Unit
) {
    ProvisionerRoute(
        appState = appState,
        provisioner = provisioner,
        otherProvisioners = otherProvisioners,
        onTtlChanged = { /*TODO*/ },
        save = save
    )
}