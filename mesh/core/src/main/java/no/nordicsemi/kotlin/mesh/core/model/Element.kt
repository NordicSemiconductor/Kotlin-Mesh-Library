@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.data.toByteArray
import no.nordicsemi.kotlin.mesh.core.layers.foundation.ConfigurationClientHandler
import no.nordicsemi.kotlin.mesh.core.layers.foundation.ConfigurationServerHandler
import no.nordicsemi.kotlin.mesh.core.layers.foundation.PrivateBeaconHandler
import no.nordicsemi.kotlin.mesh.core.layers.foundation.SceneClientHandler
import no.nordicsemi.kotlin.mesh.core.model.serialization.LocationAsStringSerializer

/**
 * Element represents a mesh element that is defined as an addressable entity within a mesh node.
 *
 * @property location           Describes the element location.
 * @property models             List of [Model] within an element.
 * @property name               A human-readable name that can identify an element within the node
 *                              and is optional according to Mesh CDB.
 * @property index              The index property contains an integer from 0 to 255 that represents
 *                              the numeric order of the element within this node and a node has
 *                              at-least one element which is called the primary element.
 * @property parentNode         Parent node that an element may belong to.
 * @property unicastAddress     Address of the element.
 * @constructor Creates an Element object.
 */
@Serializable
data class Element(
    @Serializable(with = LocationAsStringSerializer::class)
    val location: Location,
    @SerialName(value = "models")
    private var _models: MutableList<Model> = mutableListOf()
) {
    var name: String? = null
        set(value) {
            name?.let {
                require(it.isNotBlank()) { "Element name cannot be blank!" }
            }
            MeshNetwork.onChange(oldValue = field, newValue = value) {
                parentNode?.network?.updateTimestamp()
            }
            field = value
        }

    // Final index will be set when Element is added to the Node.
    // Refer https://github.com/NordicSemiconductor/IOS-nRF-Mesh-Library/blob/
    // c1755555f76fb6f393bfdad37a23566ddd581536/nRFMeshProvision/Classes/Mesh%20Model/Node.swift#L620
    var index: Int = 0
        internal set

    val models: List<Model>
        get() = _models

    @Transient
    internal var parentNode: Node? = null

    val unicastAddress: UnicastAddress
        get() = parentNode?.run {
            primaryUnicastAddress + index
        } ?: UnicastAddress(address = index + 1)

    val isPrimary: Boolean
        get() = index == 0

    internal val composition: ByteArray
        get() {
            var data = location.value.toByteArray()
            val sigModels = mutableListOf<Model>()
            val vendorModels = mutableListOf<Model>()
            for (model in _models) {
                if (model.isBluetoothSigAssigned) {
                    sigModels.add(model)
                } else {
                    vendorModels.add(model)
                }
            }
            data += sigModels.size.toByte()
            data += vendorModels.size.toByte()
            for (model in sigModels) {
                data += (model.modelId as SigModelId).modelIdentifier.toByteArray()
            }
            for (model in vendorModels) {
                val modelId = model.modelId as VendorModelId
                data += modelId.companyIdentifier.toByteArray()
                data += modelId.modelIdentifier.toByteArray()
            }
            return byteArrayOf()
        }

    init {
        require(index in LOWER_BOUND..HIGHER_BOUND) {
            " Index must be a value ranging from $LOWER_BOUND to $HIGHER_BOUND!"
        }
    }

    /**
     * Adds a model to the element.
     *
     * @param model Model to be added.
     * @return true if the model was added, false otherwise.
     */
    internal fun add(model: Model) = _models.add(model).also {
        model.parentElement = this
    }

    /**
     * Inserts a model to the element at the given index.
     *
     * @param model Model to be added.
     * @param index Index at which the model should be added.
     * @return true if the model was added, false otherwise.
     */
    internal fun insert(model: Model, index: Int) = _models.add(index, model).also {
        model.parentElement = this
    }

    /**
     * Adds the natively supported Models to the Element.
     *
     * Note: This is only to be called for the primary element of the Local Node.
     *
     * @param meshNetwork Mesh network.
     */
    internal fun addPrimaryElementModels(meshNetwork: MeshNetwork) {
        require(isPrimary) { return }
        insert(
            model = Model(
                modelId = SigModelId(Model.CONFIGURATION_SERVER_MODEL_ID),
                handler = ConfigurationServerHandler(meshNetwork = meshNetwork)
            ),
            index = 0
        )
        insert(
            model = Model(
                modelId = SigModelId(Model.CONFIGURATION_CLIENT_MODEL_ID),
                handler = ConfigurationClientHandler(meshNetwork = meshNetwork)
            ),
            index = 1
        )
        insert(Model(modelId = SigModelId(Model.HEALTH_SERVER_MODEL_ID)), index = 2)
        insert(Model(modelId = SigModelId(Model.HEALTH_CLIENT_MODEL_ID)), index = 3)
        insert(
            model = Model(
                modelId = SigModelId(Model.PRIVATE_BEACON_CLIENT_MODEL_ID),
                handler = PrivateBeaconHandler(meshNetwork = meshNetwork)
            ),
            index = 4
        )
        insert(
            model = Model(
                modelId = SigModelId(Model.SCENE_CLIENT_MODEL_ID),
                handler = SceneClientHandler(meshNetwork = meshNetwork)
            ),
            index = 5
        )
    }

    /**
     * Removes the models that are or should be supported by the library.
     */
    internal fun removePrimaryElementModels() {
        _models.removeAll { model ->
            // Health models are not yet supported.
            !model.isHealthServer && !model.isHealthClient &&
                    // The library supports Scene Client model natively.
                    !model.isSceneClient &&
                    // The models that require Device Key should not be managed by users.
                    // Some of them are supported natively in the library.
                    !model.requiresDeviceKey
        }
    }

    internal companion object {
        const val LOWER_BOUND = 0
        const val HIGHER_BOUND = 255

        /**
         * Primary Element for the Provisioner's Node.
         *
         * The Element will contain all mandatory Models (Configuration Server and Health Server)
         * and supported client models (Configuration Client, Health Client and Scene Client).
         */
        var primaryElement = Element(location = Location.UNKNOWN).apply {
            name = "Primary Element"
            // Configuration Server is required for all nodes.
            add(Model(modelId = SigModelId(Model.CONFIGURATION_SERVER_MODEL_ID)))
            // Configuration Client is added because this is a Provisioner's Node.
            add(Model(modelId = SigModelId(Model.CONFIGURATION_CLIENT_MODEL_ID)))
            // Configuration Server is required for all nodes.
            add(Model(modelId = SigModelId(Model.HEALTH_SERVER_MODEL_ID)))
            // Health Client is added because this is a Provisioner's Node.
            add(Model(modelId = SigModelId(Model.HEALTH_CLIENT_MODEL_ID)))
        }
    }
}

/**
 * Constructs the composition returned by the Composition Data Page 0 fir a given list of elements.
 *
 * @receiver List of elements.
 * @return Byte array containing the composition of a list of elements.
 */
internal fun List<Element>.composition(): ByteArray {
    var data = byteArrayOf()
    for (element in this) {
        data += element.composition
    }
    return data
}