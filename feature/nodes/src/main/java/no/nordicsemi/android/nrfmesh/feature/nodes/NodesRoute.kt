@file:Suppress("UNUSED_PARAMETER")

package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshNodeItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNodeItem1
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier

@Composable
internal fun NodesRoute(
    appState: AppState,
    uiState: NodesScreenUiState,
    navigateToNode: (Node) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit,
) {
    NodesScreen(
        uiState = uiState,
        navigateToNode = navigateToNode,
        onSwiped = onSwiped,
        onUndoClicked = onUndoClicked,
        remove = remove
    )
}

@Composable
private fun NodesScreen(
    uiState: NodesScreenUiState,
    navigateToNode: (Node) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    when (uiState.nodes.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.AutoAwesome,
            title = stringResource(R.string.no_nodes_currently_added)
        )

        false -> Nodes(
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState,
            nodes = uiState.nodes,
            navigateToNode = navigateToNode,
            onSwiped = onSwiped,
            onUndoClicked = onUndoClicked,
            remove = remove
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalStdlibApi::class)
@Composable
private fun Nodes(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    nodes: List<Node>,
    navigateToNode: (Node) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = 8.dp),
        maxItemsInEachRow = 5,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        nodes.forEach { node ->
            MeshNodeItem(
                nodeName = node.name,
                addressHex = "0x${node.primaryUnicastAddress.address.toHexString()}",
                companyName = node.companyIdentifier?.let {
                    CompanyIdentifier.name(it) ?: "Unknown"
                } ?: "Unknown",
                elements = node.elementsCount,
                models = node.elements.flatMap { it.models }.size,
                onClick = { navigateToNode(node) },
            )
        }
    }
}
