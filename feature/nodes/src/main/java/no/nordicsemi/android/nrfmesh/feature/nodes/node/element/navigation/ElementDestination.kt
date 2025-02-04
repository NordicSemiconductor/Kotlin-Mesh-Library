package no.nordicsemi.android.nrfmesh.feature.nodes.node.element.navigation

import androidx.compose.runtime.Composable
import no.nordicsemi.kotlin.mesh.core.model.Element
import no.nordicsemi.kotlin.mesh.core.model.Model

@Composable
fun ElementScreenRoute(
    element: Element,
    highlightSelectedItem: Boolean,
    navigateToModel: (Model) -> Unit,
    save: () -> Unit,
) {
    no.nordicsemi.android.nrfmesh.feature.nodes.node.element.ElementRoute(
        element = element,
        highlightSelectedItem = highlightSelectedItem,
        navigateToModel = navigateToModel,
        save = save
    )
}