@file:Suppress("UNUSED_PARAMETER")
@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)

package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.MeshNodeItem
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.toHex
import no.nordicsemi.kotlin.mesh.core.util.CompanyIdentifier
import java.util.UUID

@Composable
internal fun NodesRoute(viewModel: NodesViewModel) {
    val uiState: NodesScreenUiState by viewModel.uiState.collectAsStateWithLifecycle()
    NodesScreen(
        uiState = uiState,
        navigateToNode = { },
        onSwiped = { },
        onUndoClicked = { },
        remove = { }
    )
}

@Composable
private fun NodesScreen(
    uiState: NodesScreenUiState,
    navigateToNode: (UUID) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit
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

@Composable
private fun Nodes(
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    nodes: List<Node>,
    navigateToNode: (UUID) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit
) {
    LazyColumn {
        items(items = nodes, key = { it.uuid }) { node ->
            NodeItem(
                node = node,
                navigateToNode = navigateToNode,
                onSwiped = onSwiped,
                onUndoClicked = onUndoClicked,
                remove = remove,
                coroutineScope = coroutineScope,
                snackbarHostState = snackbarHostState
            )
        }
    }
}

@Composable
private fun NodeItem(
    node: Node,
    navigateToNode: (UUID) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    MeshNodeItem(
        nodeName = node.name,
        addressHex = node.primaryUnicastAddress.address.toHex(prefix0x = true),
        companyName = node.companyIdentifier?.let {
            CompanyIdentifier.name(it) ?: "Unknown"
        } ?: "Unknown",
        elements = node.elementsCount,
        models = node.elements.flatMap { it.models }.size,
    ) {

    }
}
