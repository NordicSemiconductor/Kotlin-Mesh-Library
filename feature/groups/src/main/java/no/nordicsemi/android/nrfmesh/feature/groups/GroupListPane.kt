package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.QuestionMark
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.common.isGenericLevelServer
import no.nordicsemi.android.nrfmesh.core.common.isGenericOnOffServer
import no.nordicsemi.android.nrfmesh.core.common.isSceneServer
import no.nordicsemi.android.nrfmesh.core.common.isSceneSetupServer
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress

@Composable
internal fun GroupListPane(
    groupInfo: GroupInfoListData,
    group: Group,
    onModelClicked: (ModelId, Int) -> Unit,
    isDetailPaneVisible: Boolean,
    selectedModelIndex: Int,
    save: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SectionTitle(
            modifier = Modifier.padding(top = 8.dp),
            title = stringResource(id = R.string.label_group)
        )
        NodeNameRow(
            name = group.name,
            onNameChanged = {
                group.name = it
                save()
            }
        )
        AddressRow(address = group.address)
        if (groupInfo.models.isNotEmpty()) {
            SectionTitle(title = stringResource(id = R.string.label_subscribed_models))
            groupInfo.models.forEach { entry ->
                ModelRow(
                    models = entry.value,
                    onModelClicked = {
                        onModelClicked(it, groupInfo.models.keys.indexOf(it))
                    },
                    isSelected = isDetailPaneVisible &&
                            groupInfo.models.keys.indexOf(entry.key) == selectedModelIndex
                )
            }
        } else {
            if (!isDetailPaneVisible) {
                SectionTitle(title = stringResource(id = R.string.label_subscribed_models))
                MeshNoItemsAvailable(
                    imageVector = Icons.Outlined.Info,
                    title = stringResource(id = R.string.label_no_models_subscribed),
                    rationale = stringResource(id = R.string.label_no_models_subscribed_rationale)
                )
            }
        }
    }
}

@Composable
private fun NodeNameRow(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        onValueChanged = onNameChanged
    )
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
private fun AddressRow(address: PrimaryGroupAddress) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Lan,
        title = stringResource(id = R.string.label_address),
        subtitle = "0x${address.address.toHexString(format = HexFormat.UpperCase)}"
    )
}

@Composable
private fun ModelRow(
    models: List<Model>,
    onModelClicked: (ModelId) -> Unit,
    isSelected: Boolean,
) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        imageVector = models.first().toIcon(),
        title = models.first().name,
        subtitle = "${models.size} models",
        onClick = { onModelClicked(models.first().modelId) }
    )
}

@Composable
private fun Model.toIcon() = if (isGenericOnOffServer()) {
    Icons.Outlined.Lightbulb
} else if (isGenericLevelServer()) {
    Icons.Outlined.LightMode
} else if (isSceneServer() || isSceneSetupServer()) {
    Icons.Outlined.Palette
} else {
    Icons.Outlined.QuestionMark
}