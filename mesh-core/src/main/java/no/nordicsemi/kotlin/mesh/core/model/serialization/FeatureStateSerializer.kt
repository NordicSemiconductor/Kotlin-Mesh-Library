package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import no.nordicsemi.kotlin.mesh.core.model.FeatureState

/**
 * Custom serializer/deserializer for [FeatureState] feature.
 */
internal object FeatureStateSerializer : KSerializer<FeatureState> {

    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(serialName = "features", kind = PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder) =
        FeatureState.from(decoder.decodeInt())

    override fun serialize(encoder: Encoder, value: FeatureState) {
        encoder.encodeInt(value = value.state)
    }
}