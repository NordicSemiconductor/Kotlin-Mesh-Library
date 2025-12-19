@file:Suppress("UNUSED_PARAMETER")

package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import no.nordicsemi.android.common.ui.view.CircularIcon
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.core.ui.MeshItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshNoItemsAvailable
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.kotlin.mesh.core.model.Node
import kotlin.uuid.ExperimentalUuidApi

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

    when (uiState.nodes.isEmpty()) {
        true -> MeshNoItemsAvailable(
            imageVector = Icons.Outlined.AutoAwesome,
            title = stringResource(R.string.no_nodes_currently_added)
        )

        false -> Nodes(
            coroutineScope = coroutineScope,
            nodes = uiState.nodes,
            navigateToNode = navigateToNode,
            onSwiped = onSwiped,
            onUndoClicked = onUndoClicked,
            remove = remove
        )
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalStdlibApi::class, ExperimentalUuidApi::class)
@Composable
private fun Nodes(
    coroutineScope: CoroutineScope,
    nodes: List<Node>,
    navigateToNode: (Node) -> Unit,
    onSwiped: (Node) -> Unit,
    onUndoClicked: (Node) -> Unit,
    remove: (Node) -> Unit,
) {
    if (isCompactWidth()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(all = 16.dp),
            content = {
                items(items = nodes, key = { it.uuid }) { node ->
                    MeshItem(
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            CircularIcon(painter = painterResource(R.drawable.ic_mesh))
                        },
                        title = node.name,
                        subtitle = node.primaryUnicastAddress.address
                            .toHexString(
                                format = HexFormat {
                                    number.prefix = "Address: 0x"
                                    upperCase = true
                                }
                            ),
                        onClick = { navigateToNode(node) },
                    )
                }
            }
        )
    } else {
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(state = rememberScrollState()),
            maxItemsInEachRow = 5,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Spacer(
                modifier = Modifier
                    .height(height = 8.dp)
                    .fillMaxWidth()
            )
            nodes.forEach { node ->
                MeshItem(
                    icon = {
                        CircularIcon(painter = painterResource(R.drawable.ic_mesh))
                    },
                    title = node.name,
                    subtitle = node.primaryUnicastAddress.address
                        .toHexString(
                            format = HexFormat {
                                number.prefix = "Address: 0x"
                                upperCase = true
                            }
                        ),
                    onClick = { navigateToNode(node) },
                )
            }
            Spacer(
                modifier = Modifier
                    .height(height = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
