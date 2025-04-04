package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.Publish

/**
 * Returns a list of groups that is not already subscribed to this model.
 */
fun Model.publishDestination() = publish?.let {
    parentElement?.parentNode?.network?.group(address = it.address.address)?.name
        ?: it.address.toHexString()
}
/**
 * Returns a list of groups that is not already subscribed to this model.
 */
fun Model.publishKey() = boundApplicationKeys.firstOrNull {
    it.index == publish?.index
} ?: throw IllegalArgumentException("Application key not found")


/**
 * Returns a list of groups that is not already subscribed to this model.
 */
fun Publish.destination(network: MeshNetwork) = this.let {
    network.group(address = it.address.address)?.name
        ?: it.address.toHexString()
}