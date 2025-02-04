package no.nordicsemi.android.nrfmesh.feature.elements.navigation

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.feature.elements.ElementRoute
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Model

@Composable
fun ElementScreenRoute(
    element: Element,
    highlightSelectedItem: Boolean,
    navigateToModel: (Model) -> Unit,
    save: () -> Unit,
) {
    ElementRoute(
        element = element,
        highlightSelectedItem = highlightSelectedItem,
        navigateToModel = navigateToModel,
        save = save
    )
}