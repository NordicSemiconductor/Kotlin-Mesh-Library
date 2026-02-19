package no.nordicsemi.android.nrfmesh.ui.network.wizard

import android.content.ContentResolver
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.core.common.Action
import no.nordicsemi.android.nrfmesh.core.common.Configuration
import no.nordicsemi.android.nrfmesh.core.common.ConfigurationProperty
import no.nordicsemi.android.nrfmesh.core.common.NetworkProperties
import no.nordicsemi.android.nrfmesh.core.common.description
import no.nordicsemi.android.nrfmesh.core.common.icon
import no.nordicsemi.android.nrfmesh.core.ui.ElevatedCardItem
import no.nordicsemi.android.nrfmesh.core.ui.MeshOutlinedButton
import no.nordicsemi.android.nrfmesh.core.ui.isCompactWidth
import no.nordicsemi.android.nrfmesh.feature.nodes.R.drawable

@Composable
fun NetworkWizardScreen(
    configurations: List<Configuration>,
    configuration: Configuration,
    onConfigurationSelected: (Configuration) -> Unit,
    add: (ConfigurationProperty) -> Unit,
    remove: (ConfigurationProperty) -> Unit,
    onContinuePressed: () -> Unit,
    importNetwork: (uri: Uri, contentResolver: ContentResolver) -> Unit,
) {
    val context = LocalContext.current
    val fileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) {
        it?.let { uri ->
            importNetwork(uri, context.contentResolver)
        } ?: onConfigurationSelected(Configuration.Empty)
    }
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(fraction = if (!isCompactWidth()) 0.4f else 1f)
                .fillMaxHeight()
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.size(size = 48.dp))
            Image(
                modifier = Modifier.size(size = 80.dp),
                painter = painterResource(drawable.ic_mesh),
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
            )
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = stringResource(R.string.label_app_welcome_rationale),
                textAlign = TextAlign.Center
            )
            Text(text = stringResource(R.string.label_start_creating))
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                configurations.forEachIndexed { index, config ->
                    SegmentedButton(
                        modifier = Modifier.defaultMinSize(minWidth = 60.dp),
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = configurations.size
                        ),
                        onClick = {
                            if (config is Configuration.Import) {
                                fileLauncher.launch("application/json")
                            }
                            onConfigurationSelected(config)
                        },
                        selected = config == configuration,
                        icon = {
                            SegmentedButtonDefaults.Icon(active = config == configuration) {
                                Icon(
                                    imageVector = config.icon(),
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        },
                        label = { Text(text = config.description()) }
                    )
                }
            }
            ConfigurationProperty.entries.forEach { property ->
                val networkProperties = configuration as NetworkProperties
                ElevatedCardItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                        .alpha(if (configuration is Configuration.Import) 0f else 1f),
                    imageVector = property.icon(),
                    title = property.description(),
                    titleAction = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (property) {
                                    ConfigurationProperty.NETWORK_KEYS -> "${networkProperties.networkKeys}"
                                    ConfigurationProperty.APPLICATION_KEYS -> "${networkProperties.applicationKeys}"
                                    ConfigurationProperty.GROUPS -> "${networkProperties.groups}"
                                    ConfigurationProperty.VIRTUAL_GROUPS -> "${configuration.virtualGroups}"
                                    ConfigurationProperty.SCENES -> "${networkProperties.scenes}"
                                }
                            )
                            when (configuration) {
                                is Configuration.Empty,
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
                    },
                    subtitle = when (configuration) {
                        is Configuration.Empty if (property == ConfigurationProperty.NETWORK_KEYS ||
                                property == ConfigurationProperty.APPLICATION_KEYS) -> "Random"

                        is Configuration.Custom if (property == ConfigurationProperty.NETWORK_KEYS ||
                                property == ConfigurationProperty.APPLICATION_KEYS) -> "Random"

                        is Configuration.Debug if (property == ConfigurationProperty.NETWORK_KEYS ||
                                property == ConfigurationProperty.APPLICATION_KEYS) -> "Fixed"

                        else -> null
                    }
                )
            }
            Spacer(modifier = Modifier.size(size = 16.dp))
            MeshOutlinedButton(
                buttonIcon = Icons.AutoMirrored.Outlined.ArrowForward,
                text = stringResource(R.string.label_continue),
                onClick = dropUnlessResumed { onContinuePressed() }
            )
            Spacer(modifier = Modifier.size(size = 72.dp))
        }
    }
}

@Composable
private fun Manage(
    configuration: Configuration,
    property: ConfigurationProperty,
    add: (ConfigurationProperty) -> Unit,
    remove: (ConfigurationProperty) -> Unit,
) {
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        Action.entries.forEach { option ->
            IconButton(
                enabled = configuration != Configuration.Empty,
                onClick = {
                    when (option) {
                        Action.ADD -> add(property)
                        Action.REMOVE -> remove(property)
                    }
                },
                content = {
                    Icon(
                        imageVector = when (option) {
                            Action.ADD -> Icons.Outlined.Add
                            Action.REMOVE -> Icons.Outlined.Remove
                        },
                        contentDescription = null
                    )
                }
            )
        }
    }
}