package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.ModelId.Companion.decode

@Serializable
data class ModelControls(val id: HexString)

@Composable
internal fun GroupDetailPane(
    content: Any?,
    network: MeshNetwork,
    group: Group,
    models: Map<ModelId, List<Model>>,
) {
    when (content) {
        is ModelControls -> GroupItems(
            modelId = content.id.decode(),
            network = network,
            group = group,
            models = models,
            send = {},
        )

        else -> GroupInfoPlaceHolder(hasModels = models.isNotEmpty())
    }
}

@Composable
internal fun GroupInfoPlaceHolder(modifier: Modifier = Modifier, hasModels: Boolean) {
    Card(
        modifier = modifier.padding(top = 40.dp, end = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(
            topStart = 24.dp,
            topEnd = 24.dp,
            bottomEnd = 0.dp,
            bottomStart = 0.dp
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                20.dp, alignment = Alignment.CenterVertically,
            ),
        ) {
            if (hasModels) {
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
    }
}