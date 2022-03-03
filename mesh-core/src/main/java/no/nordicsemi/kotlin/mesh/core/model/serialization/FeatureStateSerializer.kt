package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.FeatureState

/**
 * Custom serializer/deserializer for [FeatureState] feature.
 */
internal object FeatureStateSerializer : KSerializer<FeatureState> {

    override val descriptor: SerialDescriptor
        get() = TODO("Not yet implemented")

    override fun deserialize(decoder: Decoder): FeatureState {
        TODO("Not yet implemented")
    }

    override fun serialize(encoder: Encoder, value: FeatureState) {
        TODO("Not yet implemented")
    }
}