@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents Bluetooth mesh model contained in an element in a node.
 *
 * @property modelId                    The [ModelId] property contains a 16-bit [SigModelId] that
 *                                      represents a
 *                                      Bluetooth SIG defined model identifier field or a 32-bit
 *                                      [VendorModelId] that represents a vendor-defined model
 *                                      identifier.
 * @property subscribe                  The subscribe property contains a list of [MeshAddress].
 * @property publish                    The publish property contains a [Publish] that describes the
 *                                      configuration of this model’s publication.
 * @property bind                       The bind property contains a list of integers that
 *                                      represents indexes of the [ApplicationKey] to which this
 *                                      model is bound. Each application key index corresponds to
 *                                      the index values of one of the application key entries in
 *                                      the node’s [ApplicationKey] list.
 * @property name                       Name of the model.
 * @property isBluetoothSigAssigned     True if the model is a Bluetooth SIG defined model
 */
@Serializable
data class Model internal constructor(
    val modelId: ModelId,
) {
    var subscribe: List<SubscriptionAddress> = listOf()
        private set
    var publish: Publish? = null
        internal set
    var bind: List<KeyIndex> = listOf()
        private set
    val name: String
        get() = from(modelId)
    val isBluetoothSigAssigned: Boolean
        get() = modelId is SigModelId

    @Transient
    internal var parentElement: Element? = null

    /**
     * Subscribe this model to a given subscription address.
     *
     * @param address Subscription address to be added.
     * @return true if the address is added or false if the address is already exists in the list.
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
     * @return true if the key index is bound or false if it's already bound.
     */
    internal fun bind(index: KeyIndex) = when {
        bind.contains(element = index) -> false
        else -> {
            bind = bind + index
            true
        }
    }

    private companion object {
        /**
         * Returns the name of the model for a given model id.
         *
         * @param modelId Model ID
         * @return name of the model
         */
        private fun from(modelId: ModelId): String =
            if (modelId.isBluetoothSIGAssigned) "Vendor Model"
            else when (modelId.modelId) {
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
    }
}

internal const val CONFIGURATION_SERVER_MODEL_ID: UShort = 0x0000u
internal const val CONFIGURATION_CLIENT_MODEL_ID: UShort = 0x0001u
internal const val HEALTH_SERVER_MODEL_ID: UShort = 0x0002u
internal const val HEALTH_CLIENT_ID: UShort = 0x0002u
