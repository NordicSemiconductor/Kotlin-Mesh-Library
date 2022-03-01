package no.nordicsemi.kotlin.mesh.core.model

/**
 * Represents a configured state of a mesh model.
 *
 * @property modelId        The [ModelId] property contains a 16-bit [SigModelId] that represents a Bluetooth SIG defined model
 *                          identifier field or a 32-bit [VendorModelId] that represents a vendor-defined model identifier.
 * @property subscribe      The subscribe property contains a list of [MeshAddress].
 * @property publish        The publish property contains a [Publish] that describes the configuration of this model’s publication.
 * @property bind           The bind property contains a list of integers that represents indexes of the [ApplicationKey] to which
 *                          this model is bound. Each application key index corresponds to the index values of one of the application
 *                          key entries in the node’s [ApplicationKey] list.
 */
data class Model internal constructor(
    val modelId: ModelId,
    internal val subscribe: List<MeshAddress>,
    val publish: Publish,
    internal val bind: List<Int>,
)