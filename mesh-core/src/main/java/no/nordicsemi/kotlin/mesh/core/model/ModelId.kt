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