@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.model.serialization.ModelIdSerializer

/**
 * Represents Model ID of a Bluetooth mesh model.
 *
 * @property id                         16-bit company identifier and the 16-bit model identifier
 *                                      where the company identifier being the 2-most significant
 *                                      bytes. In the case of a Bluetooth SIG defined model, the
 *                                      company identifier is 0.
 * @property isBluetoothSigAssigned     True if the model is a Bluetooth SIG defined model.
 */
@Serializable(with = ModelIdSerializer::class)
sealed class ModelId {
    @SerialName(value = "modelId")
    abstract val id: UInt
    val isBluetoothSigAssigned: Boolean
        get() = this is SigModelId

    /**
     * Converts ModelID to hex.
     *
     * @param prefix0x If true prefixes hex value with 0x.
     */
    fun toHex(prefix0x: Boolean = false) = when (id and 0xFFFF0000u) {
        0u -> "%04X".format(id.toShort())
        else -> "%08X".format(id.toInt())
    }.also {
        return if (prefix0x) "0x$it" else it
    }

    companion object {

        /**
         * Converts a [HexString] encoded model ID to a [ModelId].
         *
         * @return [ModelId] instance.
         * @throws IllegalArgumentException If the model ID is invalid.
         */
        @Throws(IllegalArgumentException::class)
        fun HexString.decode(): ModelId = runCatching {
            this.toUInt(radix = 16).let { modelId ->
                when (modelId and 0xFFFF0000u) {
                    0u -> SigModelId(modelIdentifier = modelId.toUShort())
                    else -> VendorModelId(id = modelId)
                }
            }
        }.getOrElse {
            throw IllegalArgumentException(
                "Error while deserializing model id $this", it
            )
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
    @SerialName(value = "modelId")
    override val id: UInt = modelIdentifier.toUInt()
}

/**
 * Wrapper class for 32-bit vendor model identifier.
 *
 * @property modelIdentifier    16-bit model identifier.
 * @property companyIdentifier  16-bit company identifier.
 */
@Serializable
data class VendorModelId internal constructor(
    @SerialName(value = "modelId")
    override val id: UInt
) : ModelId() {
    val companyIdentifier: UShort = ((id and 0xFFFF0000u) shr 16).toUShort()
    val modelIdentifier: UShort = (id and 0x0000FFFFu).toUShort()

    constructor(
        modelIdentifier: UShort,
        companyIdentifier: UShort
    ) : this((modelIdentifier.toUInt()) or (companyIdentifier.toUInt() shl 16))
}