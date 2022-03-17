package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import no.nordicsemi.kotlin.mesh.core.model.*

/**
 * Custom serializer/deserializer for Features.
 */
internal object FeaturesSerializer : KSerializer<Features> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(serialName = "Features", PrimitiveKind.INT)

    override fun deserialize(decoder: Decoder): Features {
        (decoder as JsonDecoder).decodeJsonElement().jsonObject.let { features ->
            return Features(
                relay = parse(features = features, key = "relay") as Relay?,
                proxy = parse(features = features, key = "proxy") as Proxy?,
                friend = parse(features = features, key = "friend") as Friend?,
                lowPower = parse(features = features, key = "lowPower") as LowPower?
            )
        }
    }

    override fun serialize(encoder: Encoder, value: Features) {
        val json = mutableMapOf<String, JsonElement>()
        value.relay?.let { json.put("relay", JsonPrimitive(it.featureState.state)) }
        value.proxy?.let { json.put("proxy", JsonPrimitive(it.featureState.state)) }
        value.friend?.let { json.put("friend", JsonPrimitive(it.featureState.state)) }
        value.lowPower?.let { json.put("lowPower", JsonPrimitive(it.featureState.state)) }
        (encoder as JsonEncoder).encodeJsonElement(JsonObject(json))
    }

    private fun parse(features: JsonObject, key: String) =
        features[key]?.let { element ->
            element.jsonPrimitive.content.toInt().let {
                when (key) {
                    "relay" -> Relay(FeatureState.from(it))
                    "proxy" -> Proxy(FeatureState.from(it))
                    "friend" -> Friend(FeatureState.from(it))
                    "lowPower" -> LowPower(FeatureState.from(it))
                    else -> throw IllegalArgumentException("Unsupported feature type!")
                }
            }
        } ?: run {
            null
        }
}