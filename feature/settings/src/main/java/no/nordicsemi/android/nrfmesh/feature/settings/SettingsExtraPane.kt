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
import no.nordicsemi.kotlin.mesh.core.model.Provisioner

@Composable
internal fun SettingsExtraPane(
    network: MeshNetwork,
    settingsListData: SettingsListData,
    content: Any?,
    moveProvisioner: (Provisioner, Int) -> Unit,
    save: () -> Unit,
) {
    when (content) {
        is ProvisionerContent -> {
            val provisioner = network.provisioners
                .firstOrNull { it.uuid.toString() == content.uuid }
                ?: return
            ProvisionerScreenRoute(
                index = network.provisioners.indexOf(element = provisioner),
                provisioner = provisioner,
                provisionerData = settingsListData.provisioners
                    .firstOrNull { it.uuid.toString() == content.uuid }
                    ?: return,
                otherProvisioners = network.provisioners.filter { it.uuid.toString() != content.uuid },
                moveProvisioner = moveProvisioner,
                save = save
            )
        }

        is NetworkKeyContent -> NetworkKeyScreenRoute(
            key = network.networkKey(index = content.keyIndex) ?: return,
            save = save
        )

        is ApplicationKeyContent -> ApplicationKeyScreenRoute(
            key = network.applicationKey(content.keyIndex) ?: return,
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