package no.nordicsemi.android.nrfmesh.feature.groups

import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.PrimaryGroupAddress

/**
 * Defines a data object that is used to display the ui state of the Node Info List.
 * @property name           Name of the node.
 * @property address        Address of the group.
 * @property models         List of models subscribed to the group.
 */
data class GroupInfoListData(
    val name: String,
    val address: PrimaryGroupAddress,
    val models: Map<ModelId, List<Model>>,
) {
    constructor(
        group: Group,
        models: Map<ModelId, List<Model>>,
    ) : this(
        name = group.name,
        address = group.address,
        models = models,
    )
}