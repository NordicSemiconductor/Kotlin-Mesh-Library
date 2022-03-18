package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import java.io.ByteArrayInputStream

internal object MeshNetworkSerializer {
    @OptIn(ExperimentalSerializationApi::class)
    private val jsonSerializer = Json {
        encodeDefaults = true       // Encodes default values of properties.
        explicitNulls = false       // Avoids encoding null values.
        ignoreUnknownKeys = true    // Ignores the keys not available in the mesh network mode.
    }

    private const val KEY_SCHEMA = "${'$'}schema"
    private const val KEY_ID = "id"
    private const val KEY_VERSION = "version"
    private const val schema = "http://json-schema.org/draft-04/schema#"
    private const val id =
        "http://www.bluetooth.com/specifications/assigned-numbers/meshprofile/cdb-schema.json#"
    private const val version = "1.0.0"

    /**
     * Deserializes the array in to a mesh network.
     *
     * @param array in to a
     */
    @OptIn(ExperimentalSerializationApi::class)
    internal fun deserialize(array: ByteArray) = jsonSerializer.run {
        val networkElement: JsonElement = decodeFromStream(ByteArrayInputStream(array)
            .also { stream -> stream.close() })
        // Validates the json with the required properties.
        networkElement.jsonObject.let { networkObject ->
            require(schema == networkObject[KEY_SCHEMA]?.jsonPrimitive?.content) { "Invalid Json schema!" }
            require(id == networkObject[KEY_ID]?.jsonPrimitive?.content) { "Invalid Json id!" }
            require(version == networkObject[KEY_VERSION]?.jsonPrimitive?.content) { "Invalid version!" }
        }
        decodeFromJsonElement<MeshNetwork>(networkElement)
    }

    /**
     * Serializes the given mesh network to a JsonObject.
     *
     * @param network MeshNetwork to be serialized.
     */
    internal fun serialize(network: MeshNetwork) = JsonObject(content = jsonSerializer.run {
        encodeToJsonElement(value = buildMap<String, JsonElement> {
            put(KEY_SCHEMA, JsonPrimitive(value = schema))
            put(KEY_ID, JsonPrimitive(value = id))
            put(KEY_VERSION, JsonPrimitive(value = version))
        }).jsonObject + encodeToJsonElement(network).jsonObject
    })
}