package no.nordicsemi.android.nrfmesh.feature.nodes

import no.nordicsemi.kotlin.mesh.core.model.Features
import no.nordicsemi.kotlin.mesh.core.model.Node
import no.nordicsemi.kotlin.mesh.core.model.Security
import no.nordicsemi.kotlin.mesh.core.model.UnicastAddress
import java.util.UUID

/**
 * Defines a data object that is used to display the ui state of the Node Info List.
 * @property name                          Name of the node.
 * @property networkKeyCount               Number of network keys in the network.
 * @property appKeyCount                   Number of application keys in the network.
 * @property elements                      List of elements in the node.
 * @property companyIdentifier             Company identifier of the node.
 * @property productIdentifier             Product identifier of the node.
 * @property versionIdentifier             Version identifier of the node.
 * @property replayProtectionCount         Minimum number of replay protection list elements.
 * @property security                      Security of the node.
 * @property defaultTtl                    Time to live of the node.
 * @property features                      Features of the node.
 * @property excluded                      True if the node is excluded from the network.
 */
data class NodeInfoListData(
    val uuid: UUID,
    val name: String,
    val networkKeyCount: Int,
    val appKeyCount: Int,
    val elements: List<ElementListData>,
    val companyIdentifier: UShort?,
    val productIdentifier: UShort?,
    val versionIdentifier: UShort?,
    val replayProtectionCount: UShort?,
    val security: Security,
    val defaultTtl: UByte?,
    val features: Features,
    val excluded: Boolean,
) {
    constructor(node: Node) : this(
        uuid = node.uuid,
        name = node.name,
        networkKeyCount = node.networkKeys.size,
        appKeyCount = node.applicationKeys.size,
        elements = node.elements.map {
            ElementListData(
                name = it.name, unicastAddress = it.unicastAddress, modelCount = it.models.size
            )
        },
        companyIdentifier = node.companyIdentifier,
        productIdentifier = node.productIdentifier,
        versionIdentifier = node.versionIdentifier,
        replayProtectionCount = node.replayProtectionCount,
        security = node.security,
        defaultTtl = node.defaultTTL,
        features = node.features,
        excluded = node.excluded
    )
}

/**
 * Defines a data object that is used to display the ui state of the Element List.
 *
 * @property name                Name of the element.
 * @property unicastAddress      Unicast address of the element.
 * @property modelCount          Number of models in the element.
 */
data class ElementListData(
    val name: String?,
    val unicastAddress: UnicastAddress,
    val modelCount: Int,
)