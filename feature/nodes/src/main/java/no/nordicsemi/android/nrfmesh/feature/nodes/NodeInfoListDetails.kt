package no.nordicsemi.android.nrfmesh.feature.nodes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.feature.elements.navigation.ElementScreenRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.ElementModelRoute
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Node

@Composable
internal fun NodeInfoListDetails(
    content: Any?,
    node: Node,
    highlightSelectedItem: Boolean,
    navigateToModel: (Model) -> Unit,
    save: () -> Unit,
) {
    when (content) {
        is ElementModelRoute -> ElementScreenRoute(
            element = node.element(address = content.address) ?: return,
            highlightSelectedItem = highlightSelectedItem,
            navigateToModel = navigateToModel,
            save = save
        )

        else -> NodeInfoPlaceHolder()
    }
}

@Composable
internal fun NodeInfoPlaceHolder(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.padding(top = 48.dp),
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
                20.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Icon(
                modifier = Modifier.size(96.dp),
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.label_select_node_item_rationale),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}