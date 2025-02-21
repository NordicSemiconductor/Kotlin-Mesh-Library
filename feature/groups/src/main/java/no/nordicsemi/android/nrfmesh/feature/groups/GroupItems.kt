package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SensorOccupied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.nordicGrass
import no.nordicsemi.android.common.theme.nordicLake
import no.nordicsemi.android.common.theme.nordicSky
import no.nordicsemi.android.common.theme.nordicSun
import no.nordicsemi.android.nrfmesh.core.common.isGenericLevelServer
import no.nordicsemi.android.nrfmesh.core.common.isGenericOnOffServer
import no.nordicsemi.android.nrfmesh.core.common.isLightLCServer
import no.nordicsemi.android.nrfmesh.core.common.isSceneServer
import no.nordicsemi.android.nrfmesh.core.common.isSceneSetupServer
import no.nordicsemi.android.nrfmesh.core.ui.SectionTitle
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GroupItems(
    network: MeshNetwork,
    group: Group,
    modelId: ModelId,
    models: Map<ModelId, List<Model>>,
    send: (UnacknowledgedMeshMessage) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        SectionTitle(
            modifier = Modifier.padding(top = 8.dp),
            title = stringResource(id = R.string.goup_controls)
        )
        FlowRow(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            maxItemsInEachRow = 5,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            network.applicationKeys.forEach { key ->
                models[modelId]?.firstOrNull {
                    it.isBoundTo(key = key)
                }?.let { model ->
                    when {
                        model.isGenericOnOffServer() -> GenericOnOffItem(key = key, send = send)
                        model.isGenericLevelServer() -> GenericLevelItem(key = key, send = send)
                        model.isSceneServer() -> SceneServerGroupItem(key = key, send = send)
                        model.isSceneSetupServer() -> SceneSetupServerGroupItem(
                            key = key,
                            send = send
                        )

                        model.isLightLCServer() -> {
                            LightLCModeGroupItem(key = key, send = send)
                            LightLCOccupancyModeGroupItem(key = key, send = send)
                            LightLCLightOnOffGroupItem(key = key, send = send)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GenericOnOffItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicGrass
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    content = {
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_on)) },
                        )
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_off)) },
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun GenericLevelItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.LightMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicSun
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    content = {
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_plus)) },
                        )
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_minus)) },
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun SceneServerGroupItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicSky
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                        onClick = { },
                        content = { Text(text = stringResource(R.string.label_recall)) }
                    )
                }
            }
        }
    )
}

@Composable
private fun SceneSetupServerGroupItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicLake
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                        onClick = { },
                        content = { Text(text = stringResource(R.string.label_store)) }
                    )
                }
            }
        }
    )
}

@Composable
private fun LightLCModeGroupItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.SensorOccupied,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicGrass
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    content = {
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_on)) },
                        )
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_off)) },
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun LightLCOccupancyModeGroupItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.SensorOccupied,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicGrass
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    content = {
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_on)) },
                        )
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_off)) },
                        )
                    }
                )
            }
        }
    )
}

@Composable
private fun LightLCLightOnOffGroupItem(
    key: ApplicationKey,
    @Suppress("UNUSED_PARAMETER") send: (UnacknowledgedMeshMessage) -> Unit,
) {
    OutlinedCard(
        modifier = Modifier.width(width = 200.dp),
        content = {
            Column(
                modifier = Modifier.padding(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = key.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(
                    modifier = Modifier.size(60.dp),
                    imageVector = Icons.Outlined.SensorOccupied,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.nordicGrass
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 16.dp,
                        alignment = Alignment.CenterHorizontally
                    ),
                    content = {
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_on)) },
                        )
                        OutlinedButton(
                            modifier = Modifier.defaultMinSize(minWidth = 68.dp),
                            onClick = { },
                            content = { Text(text = stringResource(R.string.label_off)) },
                        )
                    }
                )
            }
        }
    )
}
