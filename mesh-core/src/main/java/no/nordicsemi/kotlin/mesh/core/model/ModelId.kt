@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.model

/**
 * Wrapper class for 16-bit or 32-bit model identifier.
 */
sealed class ModelId(internal val modelId: UInt)

/**
 * Wrapper class for 16-bit Bluetooth SIG model identifier.
 *
 * @property modelIdentifier 16-bit model identifier.
 */
data class SigModelId(val modelIdentifier: UShort) : ModelId(modelId = modelIdentifier.toUInt())

/**
 * Wrapper class for 32-bit vendor model identifier.
 *
 * @property modelIdentifier 32-bit model identifier.
 */
data class VendorModelId(val modelIdentifier: UShort, val companyIdentifier: UShort) :
    ModelId(modelId = (modelIdentifier.toUInt()) or (companyIdentifier.toUInt() shl 16))