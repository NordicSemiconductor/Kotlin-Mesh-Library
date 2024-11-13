package no.nordicsemi.android.nrfmesh.feature.configurationserver

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Numbers
import androidx.compose.material.icons.outlined.Work
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier


@Composable
internal fun CommonInformation(model: Model) {
    NameRow(name = model.name)
    ModelIdRow(modelId = model.modelId.toHex(prefix0x = true))
    Company(modelId = model.modelId)
}

@Composable
private fun NameRow(name: String) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(R.string.label_name),
        subtitle = name
    )
}

@Composable
private fun ModelIdRow(modelId: String) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Numbers,
        title = stringResource(R.string.label_model_identifier),
        subtitle = modelId
    )
}

@Composable
private fun Company(modelId: ModelId) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(top = 8.dp)
            .padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Work,
        title = stringResource(id = R.string.label_company),
        subtitle = when (modelId) {
            is SigModelId -> "Bluetooth SIG"
            is VendorModelId -> CompanyIdentifier.name(id = modelId.modelIdentifier)
                ?: stringResource(id = R.string.label_unknown)
        }
    )
}