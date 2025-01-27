package no.nordicsemi.android.nrfmesh.feature.settings

import androidx.compose.runtime.Composable
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyRoute
import no.nordicsemi.android.nrfmesh.feature.application.keys.navigation.ApplicationKeyScreenRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyRoute
import no.nordicsemi.android.nrfmesh.feature.network.keys.navigation.NetworkKeyScreenRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerRoute
import no.nordicsemi.android.nrfmesh.feature.provisioners.navigation.ProvisionerScreenRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneRoute
import no.nordicsemi.android.nrfmesh.feature.scenes.navigation.SceneScreenRoute
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork

@Composable
internal fun SettingsListDetailsExtra(
    network: MeshNetwork,
    content: Any?,
    save: () -> Unit,
) {
    when (content) {
        is ProvisionerRoute -> ProvisionerScreenRoute(
            provisioner = network.provisioners.firstOrNull { it.uuid == content.uuid } ?: return,
            otherProvisioners = network.provisioners.filter { it.uuid != content.uuid },
            save = save
        )

        is NetworkKeyRoute -> NetworkKeyScreenRoute(
            key = network.networkKeys.first { it.index == content.keyIndex },
            save = save
        )

        is ApplicationKeyRoute -> ApplicationKeyScreenRoute(
            key = network.applicationKeys.first { it.index == content.keyIndex },
            networkKeys = network.networkKeys,
            save = save
        )

        is SceneRoute -> SceneScreenRoute(
            scene = network.scenes.first { it.number == content.number },
            save = save
        )

        else -> SettingsPlaceHolder()
    }
}