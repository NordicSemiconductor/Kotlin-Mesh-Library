package no.nordicsemi.android.nrfmesh.feature.settings

import kotlin.time.Instant
import no.nordicsemi.android.nrfmesh.core.data.models.ApplicationKeyData
import no.nordicsemi.android.nrfmesh.core.data.models.NetworkKeyData
import no.nordicsemi.android.nrfmesh.core.data.models.ProvisionerData
import no.nordicsemi.android.nrfmesh.core.data.models.SceneData
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import kotlin.time.ExperimentalTime

/**
 * Defines a data object that is used to display the ui state of the Settings List.
 *
 * @param name             Name of the network.
 * @param provisioners     Provisioners in the network.
 * @param networkKeys      Network keys in the network.
 * @param appKeys          Application keys in the network.
 * @param scenes           Scenes in the network.
 * @param timestamp        Timestamp when the network was last modified.
 */

@OptIn(ExperimentalTime::class)
data class SettingsListData(
    val name: String,
    val provisioners: List<ProvisionerData>,
    val networkKeys: List<NetworkKeyData>,
    val appKeys: List<ApplicationKeyData>,
    val scenes: List<SceneData>,
    val timestamp: Instant,
) {
    /**
     * Constructs a [SettingsListData] object from the given [MeshNetwork].
     */
    constructor(network: MeshNetwork) : this(
        name = network.name,
        provisioners = network.provisioners.map { ProvisionerData(it) },
        networkKeys = network.networkKeys.map { NetworkKeyData(it) },
        appKeys = network.applicationKeys.map { ApplicationKeyData(it) },
        scenes = network.scenes.map { SceneData(it) },
        timestamp = network.timestamp
    )
}
