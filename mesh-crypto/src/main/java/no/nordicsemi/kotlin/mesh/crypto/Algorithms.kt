@file:Suppress("ClassName", "unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.crypto

/**
 * Algorithm used for calculating a Device Key.
 *
 * @property length        Length of the algorithm.
 * @property value         Value of the algorithm.
 */
enum class Algorithm {

    /**
     * `FIPS_P256_ELLIPTIC_CURVE` algorithm will be used to calculate the shared secret.
     * This has been renamed to `BTM_ECDH_P256_CMAC_AES128_AES_CCM` in Mesh Protocol 1.1.
     */
    @Deprecated(
        message = "Renamed to BTM_ECDH_P256_CMAC_AES128_AES_CCM in Mesh Protocol 1.1",
        replaceWith = ReplaceWith("BTM_ECDH_P256_CMAC_AES128_AES_CCM"),
        level = DeprecationLevel.WARNING
    )
    FIPS_P256_ELLIPTIC_CURVE,

    /**
     * `BTM_ECDH_P256_CMAC_AES128_AES_CCM` algorithm will be used to calculate the shared secret.
     */
    BTM_ECDH_P256_CMAC_AES128_AES_CCM,

    /**
     * `BTM_ECDH_P256_HMAC_SHA256_AES_CCM` algorithm will be used to calculate the shared secret.
     */
    BTM_ECDH_P256_HMAC_SHA256_AES_CCM;

    val length: Int
        get() = when (this) {
            FIPS_P256_ELLIPTIC_CURVE, BTM_ECDH_P256_CMAC_AES128_AES_CCM -> 128
            BTM_ECDH_P256_HMAC_SHA256_AES_CCM -> 256
        }

    val value: UByte
        get() = when (this) {
            FIPS_P256_ELLIPTIC_CURVE, BTM_ECDH_P256_CMAC_AES128_AES_CCM -> 0x00u
            BTM_ECDH_P256_HMAC_SHA256_AES_CCM -> 0x01u
        }


    companion object {
        /**
         * Returns the algorithm from the given provisioning pdu.
         *
         * @param pdu The provisioning pdu.
         */
        fun from(pdu: ByteArray): Algorithm? = when (pdu[1]) {
            0x00.toByte() -> BTM_ECDH_P256_CMAC_AES128_AES_CCM
            0x01.toByte() -> BTM_ECDH_P256_HMAC_SHA256_AES_CCM
            else -> null
        }
    }
}

/**
 * A set of algorithms supported by the unprovisioned device.
 *
 * @property rawValue The raw value of the algorithm.
 */
sealed class Algorithms(val rawValue: UShort) {
    private constructor(rawValue: Int) : this(rawValue.toUShort())

    @Deprecated(
        message = "Renamed to BTM_ECDH_P256_CMAC_AES128_AES_CCM in Mesh Protocol 1.1",
        replaceWith = ReplaceWith("BTM_ECDH_P256_CMAC_AES128_AES_CCM"),
        level = DeprecationLevel.WARNING
    )
    object FIPS_P256_ELLIPTIC_CURVE : Algorithms(rawValue = 1 shl 0)
    object BTM_ECDH_P256_CMAC_AES128_AES_CCM : Algorithms(rawValue = 1 shl 0)
    object BTM_ECDH_P256_HMAC_SHA256_AES_CCM : Algorithms(rawValue = 1 shl 1)

    /**
     * Returns the name of the given algorithm.
     */
    fun name(): String = when (this) {
        FIPS_P256_ELLIPTIC_CURVE, BTM_ECDH_P256_CMAC_AES128_AES_CCM ->
            "BTM ECDH P256 CMAC AES128 AES CCM"
        BTM_ECDH_P256_HMAC_SHA256_AES_CCM ->
            "BTM ECDH P256 HMAC SHA256 AES CCM"
    }


    companion object {

        /**
         * Returns the algorithm from the given value.
         *
         * @param value value of the algorithm.
         * @return supported algorithm.
         * @throws IllegalArgumentException if the algorithm is not supported.
         */
        @Throws(IllegalArgumentException::class)
        fun from(value: Short) = when (value) {
            0x01.toShort() -> BTM_ECDH_P256_CMAC_AES128_AES_CCM
            0x02.toShort() -> BTM_ECDH_P256_HMAC_SHA256_AES_CCM
            else -> throw IllegalArgumentException("Algorithm not supported")
        }
    }
}
