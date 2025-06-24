package no.nordicsemi.android.nrfmesh.feature.groups

import android.os.Parcelable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.PlaceHolder
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.ModelId.Companion.decode

@Parcelize
data class ModelControls(val id: HexString) : Parcelable

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
internal fun GroupDetailPane(
    content: Any?,
    network: MeshNetwork,
    models: Map<ModelId, List<Model>>,
    send: (UnacknowledgedMeshMessage, ApplicationKey) -> Unit
) {
    when (content) {
        is ModelControls -> GroupItems(
            modelId = content.id.decode(),
            network = network,
            models = models,
            send = send
        )

        else -> PlaceHolder(
            content = {
                if (models.isNotEmpty()) {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.Info,
                        title = stringResource(id = R.string.label_no_models_selected),
                        rationale = stringResource(id = R.string.label_select_model_rationale)
                    )
                } else {
                    MeshNoItemsAvailable(
                        imageVector = Icons.Outlined.Info,
                        title = stringResource(id = R.string.label_no_models_subscribed),
                        rationale = stringResource(id = R.string.label_no_models_subscribed_rationale)
                    )
                }
            }
        )
    }
}