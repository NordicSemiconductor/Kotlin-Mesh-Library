@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.provisioning


/**
 * Set of errors which may be reported by an unprovisioned device during provisioning process.
 */
enum class RemoteProvisioningError(val errorCode: Int) {

    /**
     * The provisioning protocol PDU is not recognized by the device.
     */
    INVALID_PDU(errorCode = 1),

    /**
     * The arguments of the protocol PDUs are outside expected values or the length of the PDU is
     * different than expected.
     */
    INVALID_FORMAT(errorCode = 2),

    /**
     * The PDU received was not expected at this moment of the procedure.
     */
    UNEXPECTED_PDU(errorCode = 3),

    /**
     * The computed confirmation value was not successfully verified.
     */
    CONFIRMATION_FAILED(errorCode = 4),

    /**
     * The provisioning protocol cannot be continued due to insufficient resources in the device.
     */
    OUT_OF_RESOURCES(errorCode = 5),

    /**
     * The Data block was not successfully decrypted.
     */
    DECRYPTION_FAILED(errorCode = 6),

    /**
     * An unexpected error occurred that may not be recoverable.
     */
    UNEXPECTED_ERROR(errorCode = 7),

    /**
     * The device cannot assign consecutive unicast addresses to all elements.
     */
    CANNOT_ASSIGN_ADDRESS(errorCode = 8),

    /**
     * The Data block contains values that cannot be accepted because of general constraints.
     */
    INVALID_ADDRESS(errorCode = 9);

    val debugDescription: String
        get() = when (this) {
            INVALID_PDU -> "Invalid PDU"
            INVALID_FORMAT -> "Invalid format"
            UNEXPECTED_PDU -> "Unexpected PDU"
            CONFIRMATION_FAILED -> "Confirmation failed"
            OUT_OF_RESOURCES -> "Out of resources"
            DECRYPTION_FAILED -> "Decryption failed"
            UNEXPECTED_ERROR -> "Unexpected error"
            CANNOT_ASSIGN_ADDRESS -> "Cannot assign addresses"
            INVALID_ADDRESS -> "Invalid data"
        }
}