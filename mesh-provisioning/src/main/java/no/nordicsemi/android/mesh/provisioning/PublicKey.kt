@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.android.mesh.provisioning

enum class PublicKey {
    NO_OOB_PUBLIC_KEY,
    OOB_PUBLIC_KEY;

    val method: PublicKeyMethod
        get() = when (this) {
            NO_OOB_PUBLIC_KEY -> PublicKeyMethod.NO_OOB_PUBLIC_KEY
            OOB_PUBLIC_KEY -> PublicKeyMethod.OOB_PUBLIC_KEY
        }
}

/**
 * The type of Device Public key to be used.
 *
 * This enumeration is used to specify the Public Key type during provisioning.
 *
 * @property NO_OOB_PUBLIC_KEY   No OOB public key is used.
 * @property OOB_PUBLIC_KEY      OOB public key is used.
 * @property value               Value of a given public key method.
 * @property debugDescription    Debug description of a given public key method.
 */
enum class PublicKeyMethod {
    NO_OOB_PUBLIC_KEY,
    OOB_PUBLIC_KEY;

    val value: UByte
        get() = when (this) {
            NO_OOB_PUBLIC_KEY -> 0x00u
            OOB_PUBLIC_KEY -> 0x01u
        }

    val debugDescription: String
        get() = when (this) {
            NO_OOB_PUBLIC_KEY -> "No OOB Public Key"
            OOB_PUBLIC_KEY -> "OOB Public Key"
        }

    internal companion object {

        /**
         * Returns the public key method from the given provisioning pdu.
         *
         * @param pdu provisioning pdu.
         */
        fun from(pdu: ProvisioningPdu): PublicKeyMethod? {
            return when (pdu[2]) {
                0x00.toByte() -> NO_OOB_PUBLIC_KEY
                0x01.toByte() -> OOB_PUBLIC_KEY
                else -> null
            }
        }
    }
}

/**
 * Type of public key information.
 *
 * @property rawValue The raw value of the public key type.
 * @constructor Creates a new PublicKeyType.
 */
sealed class PublicKeyType(private val rawValue: UByte) {
    private constructor(rawValue: Int) : this(rawValue.toUByte())

    /**
     * Public key OOB information is available.
     */
    object PublicKeyOobInformationAvailable : PublicKeyType(rawValue = 1 shl 0)

    companion object {

        /**
         * Returns the name of the given public key type.
         *
         * @param rawValue Raw value of the public key type.
         */
        fun name(rawValue: UByte): String = when (rawValue) {
            0.toUByte() -> "None"
            else -> "Public OOB Key Information Available"
        }
    }
}