package no.nordicsemi.android.nrfmesh.core.data.models

import no.nordicsemi.kotlin.mesh.core.model.GroupRange
import no.nordicsemi.kotlin.mesh.core.model.Provisioner
import no.nordicsemi.kotlin.mesh.core.model.SceneRange
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import no.nordicsemi.kotlin.mesh.core.model.UnicastRange
import java.util.UUID

/**
 * ProvisionerData is a data class that represents a Provisioner in the Mesh network.
 */
data class ProvisionerData(
    val name: String,
    val uuid: UUID,
    val address: UnicastAddress?,
    val ttl: Int,
    val deviceKey: String? = null,
    val unicastRanges: List<UnicastRange> = emptyList(),
    val groupRanges: List<GroupRange> = emptyList(),
    val sceneRanges: List<SceneRange> = emptyList(),
    val hasConfigurationCapabilities: Boolean = false
) {
    @OptIn(ExperimentalStdlibApi::class)
    constructor(provisioner: Provisioner) : this(
        name = provisioner.name,
        uuid = provisioner.uuid,
        address = provisioner.node?.primaryUnicastAddress,
        ttl = provisioner.node?.defaultTTL?.toInt() ?: 0,
        deviceKey = provisioner.node?.deviceKey?.toHexString()?.uppercase(),
        unicastRanges = provisioner.allocatedUnicastRanges.toList(),
        groupRanges = provisioner.allocatedGroupRanges,
        sceneRanges = provisioner.allocatedSceneRanges,
        hasConfigurationCapabilities = provisioner.hasConfigurationCapabilities
    )
}