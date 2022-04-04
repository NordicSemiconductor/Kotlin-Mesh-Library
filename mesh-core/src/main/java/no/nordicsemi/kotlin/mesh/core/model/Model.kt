@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable

/**
 * Represents Bluetooth mesh model contained in an element in a node.
 *
 * @property modelId        The [ModelId] property contains a 16-bit [SigModelId] that represents a Bluetooth SIG defined model
 *                          identifier field or a 32-bit [VendorModelId] that represents a vendor-defined model identifier.
 * @property subscribe      The subscribe property contains a list of [MeshAddress].
 * @property publish        The publish property contains a [Publish] that describes the configuration of this model’s publication.
 * @property bind           The bind property contains a list of integers that represents indexes of the [ApplicationKey] to which
 *                          this model is bound. Each application key index corresponds to the index values of one of the application
 *                          key entries in the node’s [ApplicationKey] list.
 */
@Serializable
data class Model internal constructor(
    val modelId: ModelId,
) {
    var subscribe: List<SubscriptionAddress> = listOf()
        private set
    var publish: Publish? = null
        internal set
    var bind: List<Int> = listOf()
        private set

    /**
     * Subscribe this model to a given subscription address.
     *
     * @param address Subscription address to be added.
     * @return        true if the address is added or false if the address is already exists in the list.
     */
    internal fun subscribe(address: SubscriptionAddress) = when {
        subscribe.contains(element = address) -> false
        else -> {
            subscribe = subscribe + address
            true
        }
    }


    /**
     * Binds the given application key index to a model.
     *
     * @param index Application key index.
     * @return      true if the key index is bound or false if it's already bound.
     */
    internal fun bind(index: Int) = when {
        bind.contains(element = index) -> false
        else -> {
            bind = bind + index
            true
        }
    }
}