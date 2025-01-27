package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.feature.provisioners.ProvisionerRoute
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import java.util.UUID

@Parcelize
data class ProvisionerRoute(val uuid: UUID) : Parcelable

@Composable
fun ProvisionerScreenRoute(
    provisioner: Provisioner,
    otherProvisioners: List<Provisioner>,
    save: () -> Unit
) {
    ProvisionerRoute(
        provisioner = provisioner,
        otherProvisioners = otherProvisioners,
        onTtlChanged = { /*TODO*/ },
        save = save
    )
}