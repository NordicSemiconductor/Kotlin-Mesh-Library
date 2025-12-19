package no.nordicsemi.android.nrfmesh.feature.provisioners.navigation

import android.os.Parcelable
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.feature.provisioners.provisioner.ProvisionerRoute
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
@Parcelize
data class ProvisionerContent(val uuid: Uuid) : Parcelable

@Composable
fun ProvisionerScreenRoute(
    snackbarHostState: SnackbarHostState,
    index: Int,
    provisioner: Provisioner,
    provisionerData: ProvisionerData,
    moveProvisioner: (Provisioner, Int) -> Unit,
    save: () -> Unit
) {
    ProvisionerRoute(
        snackbarHostState = snackbarHostState,
        index = index,
        provisioner = provisioner,
        provisionerData = provisionerData,
        moveProvisioner = moveProvisioner,
        save = save
    )
}

