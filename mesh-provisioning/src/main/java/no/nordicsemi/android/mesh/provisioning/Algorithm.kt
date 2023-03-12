@file:Suppress("ClassName", "unused")

package no.nordicsemi.android.mesh.provisioning

/**
 * A set of algorithms supported by the unprovisioned device.
 *
 * @property rawValue The raw value of the algorithm.
 */
sealed class Algorithm(private val rawValue: UShort) {
    private constructor(rawValue: Int) : this(rawValue.toUShort())

    object FIPS_P256_ELLIPTIC_CURVE : Algorithm(rawValue = 1 shl 0)
    object BTM_ECDH_P256_CMAC_AES128_AES_CCM : Algorithm(rawValue = 1 shl 0)
    object BTM_ECDH_P256_HMAC_SHA256_AES_CCM : Algorithm(rawValue = 1 shl 1)

    val value: UByte
        get() = when (this) {
            FIPS_P256_ELLIPTIC_CURVE, BTM_ECDH_P256_CMAC_AES128_AES_CCM -> 0x00u
            BTM_ECDH_P256_HMAC_SHA256_AES_CCM -> 0x01u
        }

    internal companion object {
        /**
         * Returns the name of the given algorithm.
         *
         * @param algorithm The algorithm to get the name of.
         */
        fun name(algorithm: Algorithm): String = when (algorithm) {
            FIPS_P256_ELLIPTIC_CURVE, BTM_ECDH_P256_CMAC_AES128_AES_CCM ->
                "BTM ECDH P256 CMAC AES128 AES CCM"
            BTM_ECDH_P256_HMAC_SHA256_AES_CCM ->
                "BTM ECDH P256 HMAC SHA256 AES CCM"
        }

        /**
         * Returns the algorithm from the given provisioning pdu.
         *
         * @param pdu The provisioning pdu.
         */
        fun from(pdu: ProvisioningPdu): Algorithm? = when (pdu[1]) {
            0x00.toByte() -> BTM_ECDH_P256_CMAC_AES128_AES_CCM
            0x01.toByte() -> BTM_ECDH_P256_HMAC_SHA256_AES_CCM
            else -> null
        }
    }
}