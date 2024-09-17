package no.nordicsemi.android.nrfmesh.feature.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Lan
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItemTextField
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementScreen
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Location
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.VendorModelId
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier

@Composable
internal fun ElementRoute(
    appState: AppState,
    uiState: ElementScreenUiState,
    onNameChanged: (String) -> Unit,
    navigateToModel: (Model) -> Unit,
    onBackPressed: () -> Unit
) {

    val elementScreen = appState.currentScreen as? ElementScreen

    LaunchedEffect(key1 = elementScreen) {
        elementScreen?.buttons?.onEach { action ->
            when (action) {
                ElementScreen.Actions.BACK -> onBackPressed()
            }
        }?.launchIn(this)
    }
    ElementScreen(
        uiState = uiState,
        onNameChanged = onNameChanged,
        navigateToModel = navigateToModel,
    )
}

@Composable
private fun ElementScreen(
    uiState: ElementScreenUiState,
    onNameChanged: (String) -> Unit,
    navigateToModel: (Model) -> Unit
) {
    when (uiState.elementState) {
        ElementState.Loading -> {}
        is ElementState.Success -> Element(
            element = uiState.elementState.element,
            onNameChanged = onNameChanged,
            navigateToModel = navigateToModel
        )

        is ElementState.Error -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.ErrorOutline,
            title = uiState.elementState.throwable.message ?: "Unknown error"
        )
    }
}

@Composable
private fun Element(
    element: Element,
    onNameChanged: (String) -> Unit,
    navigateToModel: (Model) -> Unit
) {
    val state = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(state = state)) {
        Spacer(modifier = Modifier.size(size = 8.dp))
        NameRow(
            name = element.name ?: stringResource(id = R.string.unknown),
            onNameChanged = onNameChanged
        )
        Spacer(modifier = Modifier.size(size = 8.dp))
        AddressRow(address = element.unicastAddress)
        Spacer(modifier = Modifier.size(size = 8.dp))
        LocationRow(location = element.location)
        SectionTitle(title = stringResource(id = R.string.title_models))
        element.models.forEachIndexed { index, model ->
            ModelRow(model = model, navigateToModel = navigateToModel)
            if (index < element.models.size - 1)
                Spacer(modifier = Modifier.size(size = 8.dp))
        }
        Spacer(modifier = Modifier.size(size = 8.dp))
    }
}

@Composable
private fun NameRow(name: String, onNameChanged: (String) -> Unit) {
    ElevatedCardItemTextField(
        modifier = Modifier.padding(horizontal = 8.dp),
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
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.Lan,
        title = stringResource(id = R.string.label_address),
        subtitle = address.toHexString(),
    )
}

@Composable
private fun LocationRow(location: Location) {
    ElevatedCardItem(
        modifier = Modifier.padding(horizontal = 8.dp),
        imageVector = Icons.Outlined.LocationOn,
        title = stringResource(id = R.string.label_location),
        subtitle = location.toString(),
    )
}

@Composable
private fun ModelRow(model: Model, navigateToModel: (Model) -> Unit) {
    ElevatedCardItem(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clickable(onClick = { navigateToModel(model) }),
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
