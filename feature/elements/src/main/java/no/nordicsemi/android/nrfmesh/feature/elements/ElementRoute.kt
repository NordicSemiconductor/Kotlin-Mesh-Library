package no.nordicsemi.android.nrfmesh.feature.elements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.data.models.ElementData
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Location
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier

@Composable
internal fun ElementRoute(
    element: Element,
    highlightSelectedItem: Boolean,
    navigateToModel: (Model) -> Unit,
    save: () -> Unit,
) {
    val elementData by remember(element.unicastAddress) {
        derivedStateOf { ElementData(element = element) }
    }
    var selectedIndex by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
    ) {
        SectionTitle(
            modifier = Modifier.padding(vertical = 8.dp),
            title = stringResource(id = R.string.label_element)
        )
        NameRow(
            name = elementData.name ?: stringResource(id = R.string.unknown),
            onNameChanged = {
                element.name = it
                save()
            }
        )
        Spacer(modifier = Modifier.size(size = 8.dp))
        AddressRow(address = element.unicastAddress)
        Spacer(modifier = Modifier.size(size = 8.dp))
        LocationRow(location = element.location)
        SectionTitle(
            modifier = Modifier.padding(vertical = 8.dp),
            title = stringResource(id = R.string.title_models)
        )
        element.models.forEachIndexed { index, model ->
            ModelRow(
                model = model,
                isSelected = index == selectedIndex && highlightSelectedItem,
                onModelClicked = {
                    selectedIndex = index
                    navigateToModel(it)
                }
            )
            if (index < element.models.size - 1)
                Spacer(modifier = Modifier.size(size = 8.dp))
        }
        Spacer(modifier = Modifier.size(size = 8.dp))
    }
}

@Composable
private fun NameRow(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Badge,
        title = stringResource(id = R.string.label_name),
        subtitle = name,
        placeholder = stringResource(id = R.string.label_placeholder_element_name),
        onValueChanged = onNameChanged
    )
}

@Composable
private fun AddressRow(address: UnicastAddress) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.Lan,
        title = stringResource(id = R.string.label_address),
        subtitle = "0x${address.toHexString()}",
    )
}

@Composable
private fun LocationRow(location: Location) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        imageVector = Icons.Outlined.LocationOn,
        title = stringResource(id = R.string.label_location),
        subtitle = location.toString(),
    )
}

@Composable
private fun ModelRow(model: Model, isSelected: Boolean, onModelClicked: (Model) -> Unit) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = when (isSelected) {
            true -> CardDefaults.outlinedCardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )

            else -> CardDefaults.outlinedCardColors()
        },
        onClick = { onModelClicked(model) },
        imageVector = Icons.Outlined.Widgets,
        title = model.name,
        subtitle = when (model.modelId) {
            is SigModelId -> "Bluetooth SIG"
            is VendorModelId -> CompanyIdentifier.name(
                id = (model.modelId as VendorModelId).companyIdentifier
            ) ?: stringResource(R.string.label_unknown_vendor)
        }
    )
}
