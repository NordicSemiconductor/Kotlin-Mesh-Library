package no.nordicsemi.android.nrfmesh.ui.network

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.feature.export.ExportOption
import no.nordicsemi.android.nrfmesh.feature.nodes.R.drawable

@Composable
fun NetworkWizard(
    configuration: Configuration = Configuration.Empty,
    add: (Configuration, ConfigurationProperty) -> Unit,
    remove: (Configuration, ConfigurationProperty) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 16.dp)
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            modifier = Modifier.size(size = 120.dp),
            painter = painterResource(drawable.ic_mesh),
            contentDescription = null
        )
        Text(
            modifier = Modifier.padding(all = 16.dp),
            text = "nRF Mesh Allows to provision, configure and control Bluetooth Mesh devices",
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier.padding(all = 16.dp),
            text = "Start by creating a new mesh network"
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            configurations.forEachIndexed { index, option ->
                SegmentedButton(
                    modifier = Modifier.defaultMinSize(minWidth = 60.dp),
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ExportOption.entries.size
                    ),
                    onClick = { },
                    selected = true,
                    icon = {
                        SegmentedButtonDefaults.Icon(active = option == configuration) {
                            Icon(
                                imageVector = option.icon(),
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    label = { Text(text = option.description()) }
                )
            }
        }
        ConfigurationProperty.entries.forEach { property ->
            ElevatedCardItem(
                modifier = Modifier.padding(horizontal = 16.dp),
                imageVector = property.icon(),
                title = property.description(),
                titleAction = {
                    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = when (property) {
                                ConfigurationProperty.NETWORK_KEYS -> "${configuration.networkKeys}"
                                ConfigurationProperty.APPLICATION_KEYS -> "${configuration.applicationKeys}"
                                ConfigurationProperty.GROUPS -> "${configuration.groups}"
                                ConfigurationProperty.VIRTUAL_GROUPS -> "${configuration.virtualGroups}"
                                ConfigurationProperty.SCENES -> "${configuration.scenes}"
                            }
                        )
                        when (configuration) {
                            is Configuration.Custom,
                            is Configuration.Debug,
                                -> {
                                Manage(
                                    configuration = configuration,
                                    property = property,
                                    add = add,
                                    remove = remove
                                )
                            }

                            else -> {}

                        }
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun NetworkWizardPreview() {
    NetworkWizard(
        configuration = Configuration.Empty,
        add = { _, _ -> },
        remove = { _, _ -> }
    )
}

@Composable
private fun Manage(
    configuration: Configuration = Configuration.Empty,
    property: ConfigurationProperty,
    add: (Configuration, ConfigurationProperty) -> Unit,
    remove: (Configuration, ConfigurationProperty) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Action.entries.forEachIndexed { index, option ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = ExportOption.entries.size
                ),
                onClick = {
                    when (option) {
                        Action.ADD -> add(configuration, property)
                        Action.REMOVE -> remove(configuration, property)
                    }
                },
                selected = true,
                icon = {
                    SegmentedButtonDefaults.Icon(active = true) {
                        Icon(
                            imageVector = Icons.Outlined.HourglassEmpty,
                            contentDescription = null,
                            modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                        )
                    }
                },
                label = { }
            )
        }
    }
}