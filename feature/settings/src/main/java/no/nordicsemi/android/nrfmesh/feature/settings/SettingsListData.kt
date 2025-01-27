package no.nordicsemi.android.nrfmesh.feature.settings

import kotlinx.datetime.Instant
import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork

/**
 * Defines a data object that is used to display the ui state of the Settings List.
 *
 * @param name                 Name of the network.
 * @param provisionerCount     Number of provisioners in the network.
 * @param networkKeyCount      Number of network keys in the network.
 * @param appKeyCount          Number of application keys in the network.
 * @param sceneCount           Number of scenes in the network.
 * @param ivIndex              IV index of the network.
 * @param timestamp            Timestamp when the network was last modified.
 */
data class SettingsListData(
    val name: String,
    val provisionerCount: Int,
    val networkKeyCount: Int,
    val appKeyCount: Int,
    val sceneCount: Int,
    val ivIndex: IvIndex,
    val timestamp: Instant,
) {
    /**
     * Constructs a [SettingsListData] object from the given [MeshNetwork].
     */
    constructor(network: MeshNetwork) : this(
        name = network.name,
        provisionerCount = network.provisioners.size,
        networkKeyCount = network.networkKeys.size,
        appKeyCount = network.applicationKeys.size,
        sceneCount = network.scenes.size,
        ivIndex = network.ivIndex,
        timestamp = network.timestamp
    )
}
