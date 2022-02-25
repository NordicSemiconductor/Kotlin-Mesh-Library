package no.nordicsemi.kotlin.mesh.core.model

/**
 * Represents a configured state of a mesh model.
 *
 * @param modelId       The [ModelId] property contains a 16-bit [SigModelId] that represents a Bluetooth SIG defined model
 *                      identifier field or a 32-bit [VendorModelId] that represents a vendor-defined model identifier.
 * @param subscribe     The subscribe property contains an array [MeshAddress].
 * @param publish       The publish property contains a [Publish] that describes the configuration of this model’s publication.
 * @param bind          The bind property contains an array of integers that represents indexes of the [ApplicationKey] to which
this model is bound. Each application key index corresponds to the index values of one of the application
key entries in the node’s [ApplicationKey] array.
 */
data class Model internal constructor(
    val modelId: ModelId,
    internal val subscribe: Array<MeshAddress>,
    val publish: Publish,
    internal val bind: Array<Int>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Model

        if (modelId != other.modelId) return false
        if (!subscribe.contentEquals(other.subscribe)) return false
        if (publish != other.publish) return false
        if (!bind.contentEquals(other.bind)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = modelId.hashCode()
        result = 31 * result + subscribe.contentHashCode()
        result = 31 * result + publish.hashCode()
        result = 31 * result + bind.contentHashCode()
        return result
    }
}