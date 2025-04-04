package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import no.nordicsemi.android.nrfmesh.core.ui.PlaceHolder
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyContent
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyContent
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyScreenRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerContent
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerScreenRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneContent
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneScreenRoute
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork

@Composable
internal fun SettingsExtraPane(
    network: MeshNetwork,
    settingsListData: SettingsListData,
    content: Any?,
    save: () -> Unit,
) {
    when (content) {
        is ProvisionerContent -> {
            ProvisionerScreenRoute(
                provisioner = network.provisioners
                    .firstOrNull { it.uuid == content.uuid }
                    ?: return,
                provisionerData = settingsListData.provisioners
                    .firstOrNull { it.uuid == content.uuid }
                    ?: return,
                otherProvisioners = network.provisioners.filter { it.uuid != content.uuid },
                save = save
            )
        }

        is NetworkKeyContent -> NetworkKeyScreenRoute(
            key = network.networkKeys.first { it.index == content.keyIndex },
            save = save
        )

        is ApplicationKeyContent -> ApplicationKeyScreenRoute(
            key = network.applicationKeys.first { it.index == content.keyIndex },
            networkKeys = network.networkKeys,
            save = save
        )

        is SceneContent -> SceneScreenRoute(
            scene = network.scenes.first { it.number == content.number },
            save = save
        )

        else -> PlaceHolder(
            modifier = Modifier.fillMaxSize(),
            imageVector = Icons.Outlined.Settings,
            text = stringResource(R.string.label_select_settings_item_rationale)
        )
    }
}