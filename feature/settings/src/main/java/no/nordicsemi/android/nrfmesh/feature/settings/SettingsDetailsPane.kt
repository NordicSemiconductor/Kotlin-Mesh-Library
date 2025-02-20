package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
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
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeysScreenRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionersScreenRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.ScenesScreenRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.SceneNumber
import java.util.UUID

@Composable
internal fun SettingsDetailsPane(
    content: Any?,
    highlightSelectedItem: Boolean,
    navigateToProvisioner: (UUID) -> Unit,
    navigateToNetworkKey: (KeyIndex) -> Unit,
    navigateToApplicationKey: (KeyIndex) -> Unit,
    navigateToScene: (SceneNumber) -> Unit,
    navigateUp: () -> Unit,
) {
    when (content) {
        is ProvisionersContent, is ProvisionerContent -> ProvisionersScreenRoute(
            highlightSelectedItem = highlightSelectedItem,
            navigateToProvisioner = navigateToProvisioner,
            navigateUp = navigateUp
        )

        is NetworkKeysContent, is NetworkKeyContent -> NetworkKeysScreenRoute(
            highlightSelectedItem = highlightSelectedItem,
            onNetworkKeyClicked = navigateToNetworkKey,
            navigateToKey = navigateToNetworkKey,
            navigateUp = navigateUp
        )

        is ApplicationKeysContent, is ApplicationKeyContent -> ApplicationKeysScreenRoute(
            highlightSelectedItem = highlightSelectedItem,
            onApplicationKeyClicked = navigateToApplicationKey,
            navigateToKey = navigateToApplicationKey,
            navigateUp = navigateUp
        )

        is ScenesContent, is SceneContent -> ScenesScreenRoute(
            highlightSelectedItem = highlightSelectedItem,
            navigateToScene = navigateToScene,
            navigateUp = navigateUp
        )

        else -> SettingsPlaceHolder()
    }
}

@Composable
internal fun SettingsPlaceHolder(modifier: Modifier = Modifier) {
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
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = stringResource(R.string.label_select_settings_item_rationale),
                style = MaterialTheme.typography.titleLarge,
            )
        }
    }
}