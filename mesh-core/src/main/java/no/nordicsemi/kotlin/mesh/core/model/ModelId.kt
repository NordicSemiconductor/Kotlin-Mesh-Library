@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.mesh.core.model.serialization.ModelIdSerializer

/**
 * Wrapper class for 16-bit or 32-bit model identifier.
 */
@Serializable(with = ModelIdSerializer::class)
sealed class ModelId {
    internal abstract val modelId: UInt

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
 * @property modelIdentifier 32-bit model identifier.
 */
@Serializable
data class VendorModelId(
    val modelIdentifier: UShort,
    val companyIdentifier: UShort
) : ModelId() {
    override val modelId = (modelIdentifier.toUInt()) or (companyIdentifier.toUInt() shl 16)
}
