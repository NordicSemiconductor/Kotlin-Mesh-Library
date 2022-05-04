@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.ModelIdSerializer

/**
 * Represents Model ID of a Bluetooth mesh model.
 *
 * @property modelId    16-bit company identifier and the 16-bit model identifier where
 *                      the company identifier being the 2-most significant bytes. In the
 *                      case of a Bluetooth SIG defined model, the company identifier is 0.
 */
@Serializable(with = ModelIdSerializer::class)
sealed class ModelId {
    internal abstract val modelId: UInt
    val isBluetoothSigAssigned: Boolean
        get() = this is SigModelId

    /**
     * Converts ModelID to hex.
     *
     * @param prefix0x If true prefixes hex value with 0x.
     */
    fun toHex(prefix0x: Boolean = false) = when (modelId and 0xFFFF0000u) {
        0u -> "%04X".format(modelId.toShort())
        else -> "%08X".format(modelId.toInt())
    }.also {
        return if (prefix0x) {
            "0x$it"
        } else {
            it
        }
    }
}

/**
 * Wrapper class for 16-bit Bluetooth SIG model identifier.
 *
 * @property modelIdentifier 16-bit model identifier.
 */
@Serializable
data class SigModelId(
    val modelIdentifier: UShort
) : ModelId() {
    override val modelId: UInt = modelIdentifier.toUInt()
}

/**
 * Wrapper class for 32-bit vendor model identifier.
 *
 * @property modelIdentifier    16-bit model identifier.
 * @property companyIdentifier  16-bit company identifier.
 */
@Serializable
data class VendorModelId internal constructor(override val modelId: UInt) : ModelId() {
    val companyIdentifier: UShort = ((modelId and 0xFFFF0000u) shr 16).toUShort()
    val modelIdentifier: UShort = (modelId and 0x0000FFFFu).toUShort()

    constructor(
        modelIdentifier: UShort,
        companyIdentifier: UShort
    ) : this((modelIdentifier.toUInt()) or (companyIdentifier.toUInt() shl 16))
}