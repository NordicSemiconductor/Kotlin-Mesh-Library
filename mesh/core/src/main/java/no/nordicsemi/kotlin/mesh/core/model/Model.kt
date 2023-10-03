@file:Suppress("MemberVisibilityCanBePrivate", "unused", "PropertyName")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import no.nordicsemi.kotlin.mesh.core.util.ModelEventHandler

/**
 * Represents Bluetooth mesh model contained in an element in a node.
 *
 * @property modelId                                    The [ModelId] property contains a 16-bit
 *                                                      [SigModelId] that represents a Bluetooth SIG
 *                                                      defined model identifier field or a 32-bit
 *                                                      [VendorModelId] that represents a
 *                                                      vendor-defined model identifier.
 * @property subscribe                                  The subscribe property contains a list of
 *                                                      [MeshAddress].
 * @property publish                                    The publish property contains a [Publish]
 *                                                      that describes the configuration of this
 *                                                      model’s publication.
 * @property bind                                       The bind property contains a list of
 *                                                      integers that represents indexes of the
 *                                                      [ApplicationKey] to which this model is
 *                                                      bound. Each application key index
 *                                                      corresponds to the index values of one of
 *                                                      the application key entries in the node’s
 *                                                      [ApplicationKey] list.
 * @property name                                       Name of the model.
 * @property isBluetoothSigAssigned                     True if the model is a Bluetooth SIG defined
 *                                                      model.
 * @property parentElement                              Parent element of the model.
 * @property boundApplicationKeys                       List of [ApplicationKey] bound to the model.
 * @property supportsApplicationKeyBinding              True if the model supports application key
 *                                                      binding.
 * @property isConfigurationServer                      True if the model is a configuration server
 *                                                      model.
 * @property isConfigurationClient                      True if the model is a configuration client
 *                                                      model.
 * @property isHealthServer                             True if the model is a health server model.
 * @property isHealthClient                             True if the model is a health client model.
 * @property isSceneClient                              True if the model is a scene client model.
 * @property isRemoteProvisioningServer                 True if the model is a remote provisioning
 *                                                      server model.
 * @property isRemoteProvisioningClient                 True if the model is a remote provisioning
 *                                                      client model.
 * @property isDirectedForwardingConfigurationServer    True if the model is a directed forwarding
 *                                                      configuration server model.
 * @property isDirectedForwardingConfigurationClient    True if the model is a directed forwarding
 *                                                      configuration client model.
 * @property isBridgeConfigurationServer                True if the model is a bridge configuration
 *                                                      server model.
 * @property isBridgeConfigurationClient                True if the model is a bridge configuration
 *                                                      client model.
 * @property isPrivateBeaconServer                      True if the model is a private beacon server
 *                                                      model.
 * @property isPrivateBeaconClient                      True if the model is a private beacon client
 *                                                      model.
 * @property isOnDemandPrivateProxyServer               True if the model is a on demand private
 *                                                      proxy server model.
 * @property isOnDemandPrivateProxyClient               True if the model is a on demand private
 *                                                      proxy client model.
 * @property isSarConfigurationServer                   True if the model is a SAR configuration
 *                                                      server model.
 * @property isSarConfigurationClient                   True if the model is a SAR configuration
 *                                                      client model.
 * @property isOpcodesAggregatorServer                  True if the model is a opcodes aggregator
 *                                                      server model.
 * @property isOpcodesAggregatorClient                  True if the model is a opcodes aggregator
 *                                                      client model.
 * @property isLargeCompositionDataServer               True if the model is a large composition
 *                                                      data server model.
 * @property isLargeCompositionDataClient               True if the model is a large composition
 *                                                      data client model.
 * @property requiresDeviceKey                          True if the model requires a device key.
 * @constructor Creates a model object.
 */
@Serializable
data class Model internal constructor(
    val modelId: ModelId,
    @SerialName(value = "bind")
    internal var _bind: MutableList<KeyIndex>,
    @SerialName(value = "subscribe")
    internal var _subscribe: MutableList<SubscriptionAddress>,
    @SerialName(value = "publish")
    internal var _publish: Publish? = null
) {
    val subscribe: List<SubscriptionAddress>
        get() = _subscribe
    val publish: Publish?
        get() = _publish
    val bind: List<KeyIndex>
        get() = _bind
    val name: String
        get() = nameOf(modelId)
    val isBluetoothSigAssigned: Boolean
        get() = modelId is SigModelId

    @Transient
    internal var parentElement: Element? = null

    val boundApplicationKeys: List<ApplicationKey>
        get() = parentElement?.parentNode?.applicationKeys?.filter { isBoundTo(it) } ?: emptyList()

    val supportsApplicationKeyBinding: Boolean
        get() = !requiresDeviceKey

    val supportsDeviceKey: Boolean
        get() = requiresDeviceKey || isOpcodesAggregatorServer || isOpcodesAggregatorClient

    val isConfigurationServer: Boolean
        get() = modelId.id == CONFIGURATION_SERVER_MODEL_ID.toUInt()
    val isConfigurationClient: Boolean
        get() = modelId.id == CONFIGURATION_CLIENT_MODEL_ID.toUInt()
    val isHealthServer: Boolean
        get() = modelId.id == HEALTH_SERVER_MODEL_ID.toUInt()
    val isHealthClient: Boolean
        get() = modelId.id == HEALTH_CLIENT_ID.toUInt()
    val isSceneClient: Boolean
        get() = modelId.id == SCENE_CLIENT_MODEL_ID.toUInt()
    val isRemoteProvisioningServer: Boolean
        get() = modelId.id == REMOTE_PROVISIONING_SERVER_MODEL_ID.toUInt()
    val isRemoteProvisioningClient: Boolean
        get() = modelId.id == REMOTE_PROVISIONING_CLIENT_MODEL_ID.toUInt()
    val isDirectedForwardingConfigurationServer: Boolean
        get() = modelId.id == DIRECTED_FORWARDING_CONFIGURATION_SERVER_MODEL_ID.toUInt()
    val isDirectedForwardingConfigurationClient: Boolean
        get() = modelId.id == DIRECTED_FORWARDING_CONFIGURATION_CLIENT_MODEL_ID.toUInt()
    val isBridgeConfigurationServer: Boolean
        get() = modelId.id == BRIDGE_CONFIGURATION_SERVER_MODEL_ID.toUInt()
    val isBridgeConfigurationClient: Boolean
        get() = modelId.id == BRIDGE_CONFIGURATION_CLIENT_MODEL_ID.toUInt()
    val isPrivateBeaconServer: Boolean
        get() = modelId.id == PRIVATE_BEACON_SERVER_MODEL_ID.toUInt()
    val isPrivateBeaconClient: Boolean
        get() = modelId.id == PRIVATE_BEACON_CLIENT_MODEL_ID.toUInt()
    val isOnDemandPrivateProxyServer: Boolean
        get() = modelId.id == ON_DEMAND_PRIVATE_PROXY_SERVER_MODEL_ID.toUInt()
    val isOnDemandPrivateProxyClient: Boolean
        get() = modelId.id == ON_DEMAND_PRIVATE_PROXY_CLIENT_MODEL_ID.toUInt()
    val isSarConfigurationServer: Boolean
        get() = modelId.id == SAR_CONFIGURATION_SERVER_MODEL_ID.toUInt()
    val isSarConfigurationClient: Boolean
        get() = modelId.id == SAR_CONFIGURATION_CLIENT_MODEL_ID.toUInt()
    val isOpcodesAggregatorServer: Boolean
        get() = modelId.id == OP_CODES_AGGREGATOR_SERVER_MODEL_ID.toUInt()
    val isOpcodesAggregatorClient: Boolean
        get() = modelId.id == OP_CODES_AGGREGATOR_CLIENT_MODEL_ID.toUInt()
    val isLargeCompositionDataServer: Boolean
        get() = modelId.id == LARGE_COMPOSITION_DATA_SERVER_MODEL_ID.toUInt()
    val isLargeCompositionDataClient: Boolean
        get() = modelId.id == LARGE_COMPOSITION_DATA_CLIENT_MODEL_ID.toUInt()

    val requiresDeviceKey: Boolean
        get() = isConfigurationServer || isConfigurationClient || isRemoteProvisioningServer ||
                isRemoteProvisioningClient || isDirectedForwardingConfigurationServer ||
                isDirectedForwardingConfigurationClient || isBridgeConfigurationServer ||
                isBridgeConfigurationClient || isPrivateBeaconServer || isPrivateBeaconClient ||
                isOnDemandPrivateProxyServer || isOnDemandPrivateProxyClient ||
                isSarConfigurationServer || isSarConfigurationClient ||
                isLargeCompositionDataServer || isLargeCompositionDataClient

    @Transient
    var eventHandler: ModelEventHandler? = null

    /**
     * Constructs a Model
     *
     * @param modelId Model ID.
     * @param handler Model event handler.
     */
    internal constructor(modelId: ModelId, handler: ModelEventHandler? = null) : this(
        modelId = modelId,
        _bind = mutableListOf(),
        _subscribe = mutableListOf(),
        _publish = null
    ) {
        eventHandler = handler
    }

    /**
     * Subscribe this model to a given subscription address.
     *
     * @param address Subscription address to be added.
     * @return true if the address is added or false if the address is already exists in the list.
     */
    internal fun subscribe(address: SubscriptionAddress) = when {
        _subscribe.contains(element = address) -> false
        else -> {
            _subscribe.add(address)
            true
        }
    }

    /**
     * Binds the given application key index to a model.
     *
     * @param index Application key index.
     * @return true if the key index is bound or false if it's already bound.
     */
    internal fun bind(index: KeyIndex) = when {
        bind.contains(element = index) -> false
        else -> {
            _bind.add(index)
            true
        }
    }

    /**
     * Copies the properties from the given model
     *
     * @param model Model to copy from
     */
    fun copyProperties(model: Model) {
        _bind = model._bind
        _publish = model._publish
        _subscribe = model._subscribe
    }

    /**
     * Checks if the given application key is bound to the model.
     *
     * @param applicationKey Application key to check.
     * @return true if the key is bound to the model or false otherwise.
     */
    fun isBoundTo(applicationKey: ApplicationKey) = bind.any { it == applicationKey.index }

    /**
     * Checks if the Model is subscribed to the given Group.
     *
     * @param group Group to check.
     * @return true if the model is subscribed to the group or false otherwise.
     */
    fun isSubscribedTo(group: Group) = isSubscribedTo(group.address)

    /**
     * Checks if the Model is subscribed to the given address.
     *
     * @param address Address to check.
     * @return true if the model is subscribed to the address or false otherwise.
     */
    fun isSubscribedTo(address: PrimaryGroupAddress) = subscribe.any { it == address }

    internal companion object {

        internal const val CONFIGURATION_SERVER_MODEL_ID: UShort = 0x0000u
        internal const val CONFIGURATION_CLIENT_MODEL_ID: UShort = 0x0001u
        internal const val HEALTH_SERVER_MODEL_ID: UShort = 0x0002u
        internal const val HEALTH_CLIENT_ID: UShort = 0x0002u

        // Configuration models added in Mesh Protocol 1.1
        internal const val REMOTE_PROVISIONING_SERVER_MODEL_ID: UShort = 0x0004u
        internal const val REMOTE_PROVISIONING_CLIENT_MODEL_ID: UShort = 0x0005u
        internal const val DIRECTED_FORWARDING_CONFIGURATION_SERVER_MODEL_ID: UShort = 0x0006u
        internal const val DIRECTED_FORWARDING_CONFIGURATION_CLIENT_MODEL_ID: UShort = 0x0007u
        internal const val BRIDGE_CONFIGURATION_SERVER_MODEL_ID: UShort = 0x0008u
        internal const val BRIDGE_CONFIGURATION_CLIENT_MODEL_ID: UShort = 0x0009u
        internal const val PRIVATE_BEACON_SERVER_MODEL_ID: UShort = 0x000Au
        internal const val PRIVATE_BEACON_CLIENT_MODEL_ID: UShort = 0x000Bu
        internal const val ON_DEMAND_PRIVATE_PROXY_SERVER_MODEL_ID: UShort = 0x000Cu
        internal const val ON_DEMAND_PRIVATE_PROXY_CLIENT_MODEL_ID: UShort = 0x000Du
        internal const val SAR_CONFIGURATION_SERVER_MODEL_ID: UShort = 0x000Eu
        internal const val SAR_CONFIGURATION_CLIENT_MODEL_ID: UShort = 0x000Fu
        internal const val OP_CODES_AGGREGATOR_SERVER_MODEL_ID: UShort = 0x0010u
        internal const val OP_CODES_AGGREGATOR_CLIENT_MODEL_ID: UShort = 0x0011u
        internal const val LARGE_COMPOSITION_DATA_SERVER_MODEL_ID: UShort = 0x0012u
        internal const val LARGE_COMPOSITION_DATA_CLIENT_MODEL_ID: UShort = 0x0013u
        internal const val SOLICITATION_PDU_RPL_CONFIGURATION_SERVER_MODEL_ID: UShort = 0x0014u
        internal const val SOLICITATION_PDU_RPL_CONFIGURATION_CLIENT_MODEL_ID: UShort = 0x0015u

        // Generics
        internal const val GENERIC_ON_OFF_SERVER_MODEL_ID: UShort = 0x1000u
        internal const val GENERIC_ON_OFF_CLIENT_MODEL_ID: UShort = 0x1001u
        internal const val GENERIC_LEVEL_SERVER_MODEL_ID: UShort = 0x1002u
        internal const val GENERIC_LEVEL_CLIENT_MODEL_ID: UShort = 0x1003u
        internal const val GENERIC_DEFAULT_TRANSITION_TIME_SERVER_MODEL_ID: UShort = 0x1004u
        internal const val GENERIC_DEFAULT_TRANSITION_TIME_CLIENT_MODEL_ID: UShort = 0x1005u
        internal const val GENERIC_POWER_ON_OFF_SERVER_MODEL_ID: UShort = 0x1006u
        internal const val GENERIC_POWER_ON_OFF_SETUP_SERVER_MODEL_ID: UShort = 0x1007u
        internal const val GENERIC_POWER_ON_OFF_CLIENT_MODEL_ID: UShort = 0x1008u
        internal const val GENERIC_POWER_LEVEL_SERVER_MODEL_ID: UShort = 0x1009u
        internal const val GENERIC_POWER_LEVEL_SETUP_SERVER_MODEL_ID: UShort = 0x100Au
        internal const val GENERIC_POWER_LEVEL_CLIENT_MODEL_ID: UShort = 0x100Bu
        internal const val GENERIC_BATTERY_SERVER_MODEL_ID: UShort = 0x100Cu
        internal const val GENERIC_BATTERY_CLIENT_MODEL_ID: UShort = 0x100Du
        internal const val GENERIC_LOCATION_SERVER_MODEL_ID: UShort = 0x100Eu
        internal const val GENERIC_LOCATION_SETUP_SERVER_MODEL_ID: UShort = 0x100Fu
        internal const val GENERIC_LOCATION_CLIENT_MODEL_ID: UShort = 0x1010u
        internal const val GENERIC_ADMIN_PROPERTY_SERVER_MODEL_ID: UShort = 0x1011u
        internal const val GENERIC_MANUFACTURER_PROPERTY_SERVER_MODEL_ID: UShort = 0x1012u
        internal const val GENERIC_USER_PROPERTY_SERVER_MODEL_ID: UShort = 0x1013u
        internal const val GENERIC_CLIENT_PROPERTY_SERVER_MODEL_ID: UShort = 0x1014u
        internal const val GENERIC_PROPERTY_CLIENT_MODEL_ID: UShort = 0x1015u

        const val SENSOR_SERVER_MODEL_ID: UShort = 0x1100u
        const val SENSOR_SETUP_SERVER_MODEL_ID: UShort = 0x1101u
        const val SENSOR_CLIENT_MODEL_ID: UShort = 0x1102u

        // Time and Scenes
        const val TIME_SERVER_MODEL_ID: UShort = 0x1200u
        const val TIME_SETUP_SERVER_MODEL_ID: UShort = 0x1201u
        const val TIME_CLIENT_MODEL_ID: UShort = 0x1202u
        const val SCENE_SERVER_MODEL_ID: UShort = 0x1203u
        const val SCENE_SETUP_SERVER_MODEL_ID: UShort = 0x1204u
        const val SCENE_CLIENT_MODEL_ID: UShort = 0x1205u
        const val SCHEDULER_SERVER_MODEL_ID: UShort = 0x1206u
        const val SCHEDULER_SETUP_SERVER_MODEL_ID: UShort = 0x1207u
        const val SCHEDULER_CLIENT_MODEL_ID: UShort = 0x1208u

        // Lighting
        const val LIGHT_LIGHTNESS_SERVER_MODEL_ID: UShort = 0x1300u
        const val LIGHT_LIGHTNESS_SETUP_SERVER_MODEL_ID: UShort = 0x1301u
        const val LIGHT_LIGHTNESS_CLIENT_MODEL_ID: UShort = 0x1302u
        const val LIGHT_CTL_SERVER_MODEL_ID: UShort = 0x1303u
        const val LIGHT_CTL_SETUP_SERVER_MODEL_ID: UShort = 0x1304u
        const val LIGHT_CTL_CLIENT_MODEL_ID: UShort = 0x1305u
        const val LIGHT_CTL_TEMPERATURE_SERVER_MODEL_ID: UShort = 0x1306u
        const val LIGHT_HSL_SERVER_MODEL_ID: UShort = 0x1307u
        const val LIGHT_HSL_SETUP_SERVER_MODEL_ID: UShort = 0x1308u
        const val LIGHT_HSL_CLIENT_MODEL_ID: UShort = 0x1309u
        const val LIGHT_HSL_HUE_SERVER_MODEL_ID: UShort = 0x130Au
        const val LIGHT_HSL_SATURATION_SERVER_MODEL_ID: UShort = 0x130Bu
        const val LIGHT_XYL_SERVER_MODEL_ID: UShort = 0x130Cu
        const val LIGHT_XYL_SETUP_SERVER_MODEL_ID: UShort = 0x130Du
        const val LIGHT_XYL_CLIENT_MODEL_ID: UShort = 0x130Eu
        const val LIGHT_LC_SERVER_MODEL_ID: UShort = 0x130Fu
        const val LIGHT_LC_SETUP_SERVER_MODEL_ID: UShort = 0x1310u
        const val LIGHT_LC_CLIENT_MODEL_ID: UShort = 0x1311u

        // BLOB Transfer
        const val BLOB_TRANSFER_SERVER: UShort = 0x1400u
        const val BLOB_TRANSFER_CLIENT: UShort = 0x1401u

        // Device Firmware Update
        const val FIRMWARE_UPDATE_SERVER: UShort = 0x1402u
        const val FIRMWARE_UPDATE_CLIENT: UShort = 0x1403u
        const val FIRMWARE_DISTRIBUTION_SERVER: UShort = 0x1404u
        const val FIRMWARE_DISTRIBUTION_CLIENT: UShort = 0x1405u

        /**
         * Returns the name of the model for a given model id.
         *
         * @param modelId Model ID
         * @return name of the model
         */
        private fun nameOf(modelId: ModelId): String =
            if (modelId.isBluetoothSigAssigned) "Vendor Model"
            else when (modelId.id) {
                // Foundation
                0x0000.toUInt() -> "Configuration Server"
                0x0001.toUInt() -> "Configuration Client"
                0x0002.toUInt() -> "Health Server"
                0x0003.toUInt() -> "Health Client"
                // Generic
                0x1000.toUInt() -> "Generic OnOff Server"
                0x1001.toUInt() -> "Generic OnOff Client"
                0x1002.toUInt() -> "Generic Level Server"
                0x1003.toUInt() -> "Generic Level Client"
                0x1004.toUInt() -> "Generic Default Transition Time Server"
                0x1005.toUInt() -> "Generic Default Transition Time Client"
                0x1006.toUInt() -> "Generic Power OnOff Server"
                0x1007.toUInt() -> "Generic Power OnOff Setup Server"
                0x1008.toUInt() -> "Generic Power OnOff Client"
                0x1009.toUInt() -> "Generic Power Level Server"
                0x100A.toUInt() -> "Generic Power Level Setup Server"
                0x100B.toUInt() -> "Generic Power Level Client"
                0x100C.toUInt() -> "Generic Battery Server"
                0x100D.toUInt() -> "Generic Battery Client"
                0x100E.toUInt() -> "Generic Location Server"
                0x100F.toUInt() -> "Generic Location Setup Server"
                0x1010.toUInt() -> "Generic Location Client"
                0x1011.toUInt() -> "Generic Admin Property Server"
                0x1012.toUInt() -> "Generic Manufacturer Property Server"
                0x1013.toUInt() -> "Generic User Property Server"
                0x1014.toUInt() -> "Generic Client Property Server"
                0x1015.toUInt() -> "Generic Property Client"
                // Sensors
                0x1100.toUInt() -> "Sensor Server"
                0x1101.toUInt() -> "Sensor Setup Server"
                0x1102.toUInt() -> "Sensor Client"
                // Time and Scenes
                0x1200.toUInt() -> "Time Server"
                0x1201.toUInt() -> "Time Setup Server"
                0x1202.toUInt() -> "Time Client"
                0x1203.toUInt() -> "Scene Server"
                0x1204.toUInt() -> "Scene Setup Server"
                0x1205.toUInt() -> "Scene Client"
                0x1206.toUInt() -> "Scheduler Server"
                0x1207.toUInt() -> "Scheduler Setup Server"
                0x1208.toUInt() -> "Scheduler Client"
                // Lighting
                0x1300.toUInt() -> "Light Lightness Server"
                0x1301.toUInt() -> "Light Lightness Setup Server"
                0x1302.toUInt() -> "Light Lightness Client"
                0x1303.toUInt() -> "Light CTL Server"
                0x1304.toUInt() -> "Light CTL Setup Server"
                0x1305.toUInt() -> "Light CTL Client"
                0x1306.toUInt() -> "Light CTL Temperature Server"
                0x1307.toUInt() -> "Light HSL Server"
                0x1308.toUInt() -> "Light HSL Setup Server "
                0x1309.toUInt() -> "Light HSL Client"
                0x130A.toUInt() -> "Light HSL Hue Server"
                0x130B.toUInt() -> "Light HSL Saturation Server"
                0x130C.toUInt() -> "Light xyL Server"
                0x130D.toUInt() -> "Light xyL Setup Server"
                0x130E.toUInt() -> "Light xyL Client"
                0x130F.toUInt() -> "Light LC Server"
                0x1310.toUInt() -> "Light LC Setup Server"
                0x1311.toUInt() -> "Light LC Client"
                else -> "Unknown"
            }

        /**
         * Constructs a model object.
         *
         * @param model                Model.
         * @param applicationKeys      List of application keys.
         * @param nodes                List of nodes.
         * @param groups               List of groups.
         *
         */
        fun init(
            model: Model,
            applicationKeys: List<ApplicationKey>,
            nodes: List<Node>,
            groups: List<Group>
        ): Model? {
            val bind = model.bind.filter { keyIndex ->
                applicationKeys.any { it.index == keyIndex }
            }
            val subscribe = model.subscribe.filter { address ->
                groups.any { group -> group.address == address }
            }

            // Copy the publish settings if
            // - it exists
            // - is configured to use one of the exported Application Keys
            // - The destination addresses is an exported Node, an exported Group or a special group
            return model.publish?.takeIf { publish ->
                applicationKeys.any { it.index == publish.index }
            }?.let { publish ->
                if (publish.address is FixedGroupAddress ||
                    (publish.address is UnicastAddress && nodes.any { node ->
                        node.elements.any { element ->
                            element.unicastAddress == publish.address
                        }
                    }) ||
                    (publish.address is GroupAddress && groups.any { it.address == publish.address })
                ) {
                    Model(
                        modelId = model.modelId,
                        _bind = bind.toMutableList(),
                        _subscribe = subscribe.toMutableList(),
                        _publish = publish
                    )
                } else {
                    null
                }
            }
        }
    }
}

/**
 * Returns the model for a given company and model identifier.
 *
 * @param companyIdentifier Company identifier
 * @param modelIdentifier   Model identifier.
 * @return Model or null if not found.
 */
fun List<Model>.model(companyIdentifier: UShort?, modelIdentifier: UShort) = model(
    if (companyIdentifier != null) VendorModelId(
        companyIdentifier = companyIdentifier,
        modelIdentifier = modelIdentifier
    ) else SigModelId(modelIdentifier = modelIdentifier)
)

/**
 * Returns the model with the given model id.
 *
 * @param modelId Model ID.
 * @return Model or null if not found.
 */
fun List<Model>.model(modelId: ModelId) = firstOrNull { it.modelId == modelId }

