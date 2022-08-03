package no.nordicsemi.kotlin.mesh.core.model.serialization

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.serialization.config.*
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
    private val VERSION_PATTERN = Regex("^[0-9]\\.[0-9]\\.[0-9]\$")

    /**
     * Deserializes the array in to a mesh network.
     *
     * @param array in to a mesh network.
     */
    @OptIn(ExperimentalSerializationApi::class)
    internal fun deserialize(array: ByteArray) = jsonSerializer.run {
        val networkElement: JsonElement = decodeFromStream(ByteArrayInputStream(array)
            .also { stream -> stream.close() })
        // Validates the json with the required properties.
        networkElement.jsonObject.let { networkObject ->
            require(schema == networkObject[KEY_SCHEMA]?.jsonPrimitive?.content) {
                "Invalid Json schema!"
            }
            // ID does not need checking as it has changed in the past and may change again.
            // require(id == networkObject[KEY_ID]?.jsonPrimitive?.content) { "Invalid Json id!" }
            require(
                networkObject[KEY_VERSION]?.jsonPrimitive?.content?.matches(VERSION_PATTERN)
                    ?: false
            ) { "Invalid version!" }
        }
        decodeFromJsonElement<MeshNetwork>(networkElement).apply {
            // Assign network reference to access parent network within the object.
            _networkKeys.forEach {
                it.network = this
            }
            _applicationKeys.forEach {
                it.network = this
            }
            _groups.forEach {
                it.network = this
            }
            _scenes.forEach {
                it.network = this
            }
            _provisioners.forEach {
                it.network = this
            }
            _nodes.forEach { node ->
                node.network = this
                node.elements.forEach { element ->
                    element.parentNode = node
                    element.models.forEach { model ->
                        model.parentElement = element
                    }
                }
            }
            _networkExclusions.forEach {
                it.network = this
            }
        }
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


    /**
     * Serializes the given mesh network to a JsonObject based on the configuration.
     *
     * @param network                      MeshNetwork to be serialized.
     * @param networkKeysConfig            Configuration of the network keys to be exported.
     * @param applicationKeysConfig        Configuration of the application keys to be exported.
     * @param provisionersConfig           Configuration of the provisioner to be exported.
     * @param nodesConfig                  Configuration of the nodes to be exported.
     * @param groupsConfig                 Configuration of the groups to be exported.
     * @param scenesConfig                 Configuration of the scenes to be exported.
     */
    internal fun serialize(
        network: MeshNetwork,
        networkKeysConfig: NetworkKeysConfig,
        applicationKeysConfig: ApplicationKeysConfig,
        provisionersConfig: ProvisionersConfig,
        nodesConfig: NodesConfig,
        groupsConfig: GroupsConfig,
        scenesConfig: ScenesConfig
    ) {
        serialize(
            network = network.apply {
                partial = true
                // List of Network Keys to export.
                includeNetKeysForExport(networkKeysConfig)
                // List of Application Keys to export.
                includeAppKeysForExport(applicationKeysConfig)

                // List of nodes to export.
                includeNodesForExport(nodesConfig)

                // List of provisioners to export.
                includeProvisionersForExport(provisionersConfig)

                // Excludes the nodes unknown to network keys.
                // TODO what will happen to the provisioner if it's node is excluded due to an unknown
                //      network key although a provisioner knows all the network keys.
                excludeNodesUnknownToNetworkKeys()
                // Exclude app keys that are bound but not in the selected application key list.
                excludeUnselectedApplicationKeys()

                includeGroupsForGroups(groupsConfig)

                // List of Scenes to export.
                includeScenesForExport(scenesConfig)

            }
        )
    }
}
