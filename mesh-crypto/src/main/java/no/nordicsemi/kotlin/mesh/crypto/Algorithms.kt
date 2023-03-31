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

        /**
         * Returns the strongest algorithm from a given list of supported algorithms.
         *
         * @receiver list of Algorithms.
         * @return strongest algorithm supported.
         */
        fun List<Algorithms>.strongest(): Algorithm = find {
            it.rawValue == Algorithms.BtmEcdhP256HmacSha256AesCcm.rawValue
        }?.let {
            BTM_ECDH_P256_HMAC_SHA256_AES_CCM
        } ?: BTM_ECDH_P256_CMAC_AES128_AES_CCM
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
    object FipsP256EllipticCurve : Algorithms(rawValue = 1 shl 0)
    object BtmEcdhP256CmacAes128AesCcm : Algorithms(rawValue = 1 shl 0)
    object BtmEcdhP256HmacSha256AesCcm : Algorithms(rawValue = 1 shl 1)

    /**
     * Returns the name of the given algorithm.
     */
    fun name(): String = when (this) {
        FipsP256EllipticCurve, BtmEcdhP256CmacAes128AesCcm ->
            "BTM ECDH P256 CMAC AES128 AES CCM"
        BtmEcdhP256HmacSha256AesCcm ->
            "BTM ECDH P256 HMAC SHA256 AES CCM"
    }


    companion object {

        private val algorithms = listOf(BtmEcdhP256CmacAes128AesCcm, BtmEcdhP256HmacSha256AesCcm)

        /**
         * Returns the list supported algorithms.
         *
         * @param supportedAlgorithms Supported algorithms from provisioning capabilities pdu.
         * @return a list of supported algorithms or an empty list if none is supported.
         */
        fun from(supportedAlgorithms: UShort): List<Algorithms> = algorithms.filter {
            it.rawValue.toInt() and supportedAlgorithms.toInt() != 0
        }

        /**
         * Converts a list of supported algorithms to a UShort value.
         *
         * @receiver List of supported algorithms.
         * @return UShort containing the raw value of the list of algorithms.
         */
        fun List<Algorithms>.toUShort(): UShort {
            var value = 0
            forEach {
                value = value or it.rawValue.toInt()
            }
            return value.toUShort()
        }

        /**
         * Returns the strongest algorithm from a given list of supported algorithms.
         *
         * @receiver list of Algorithms.
         * @return strongest algorithm supported.
         */
        fun List<Algorithms>.strongest(): Algorithms = find {
            it.rawValue == BtmEcdhP256HmacSha256AesCcm.rawValue
        } ?: BtmEcdhP256CmacAes128AesCcm
    }
}
