@file:Suppress("unused", "MemberVisibilityCanBePrivate", "ArrayInDataClass")

package no.nordicsemi.kotlin.mesh.provisioning

import no.nordicsemi.kotlin.mesh.core.exception.InvalidKeyLength
import no.nordicsemi.kotlin.mesh.provisioning.PublicKeyMethod.NO_OOB_PUBLIC_KEY
import no.nordicsemi.kotlin.mesh.provisioning.PublicKeyMethod.OOB_PUBLIC_KEY

/**
 * Defines Public Key type that's supported by the device. This field is received as a part of the
 * Provisioning Capabilities PDU.
 *
 * @property method Method of the public key.
 */
sealed class PublicKey {

    /**
     * No OOB public key is used.
     */
    object NoOobPublicKey : PublicKey()

    /**
     * OOB public key is used.
     *
     * @property key OOB public key.
     */
    data class OobPublicKey(val key: ByteArray) : PublicKey() {
        init {
            require(key.size == 16) { throw InvalidKeyLength() }
        }
    }

    val method: PublicKeyMethod
        get() = when (this) {
            NoOobPublicKey -> NO_OOB_PUBLIC_KEY
            is OobPublicKey -> OOB_PUBLIC_KEY
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
sealed class PublicKeyType(val rawValue: UByte) {
    constructor(rawValue: Int) : this(rawValue.toUByte())

    /**
     * Public key OOB information is available.
     */
    object PublicKeyOobInformationAvailable : PublicKeyType(rawValue = 1 shl 0) {
        override fun toString() = "Public OOB Key Information Available"
    }

    companion object {
        val publicKeyTypes = listOf(PublicKeyOobInformationAvailable)

        /**
         * Returns the name of the given public key type.
         *
         * @param rawValue Raw value of the public key type.
         */
        fun name(rawValue: UByte): String = when (rawValue) {
            0.toUByte() -> "None"
            else -> "Public OOB Key Information Available"
        }

        /**
         * Returns a list of public key information based on the give value.
         *
         * @param value Supported public key types value obtained from the provisioning
         *              capabilities pdu.
         * @return List a of supported public key type or empty if oob information is not available.
         */
        @Throws(IllegalArgumentException::class)
        fun from(value: UByte) = publicKeyTypes.filter {
            it.rawValue.toInt() and value.toInt() != 0
        }

        /**
         * Converts a list of supported public key types to a UByte value.
         *
         * @receiver List of public key types.
         * @return UByte containing the raw value of the list of public key types.
         */
        fun List<PublicKeyType>.toByte(): Byte {
            var value = 0
            forEach {
                value = value or it.rawValue.toInt()
            }
            return value.toByte()
        }
    }
}