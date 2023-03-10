@file:Suppress("unused")

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
 * @property NO_OOB_PUBLIC_KEY No OOB public key is used.
 * @property OOB_PUBLIC_KEY OOB public key is used.
 */
enum class PublicKeyMethod {
    NO_OOB_PUBLIC_KEY,
    OOB_PUBLIC_KEY;

    internal companion object {

        /**
         * Returns the name of the given public key method.
         *
         * @param publicKey The public key method.
         */
        fun name(publicKey: PublicKeyMethod): String = when (publicKey) {
            NO_OOB_PUBLIC_KEY -> "No OOB Public Key"
            OOB_PUBLIC_KEY -> "OOB Public Key"
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