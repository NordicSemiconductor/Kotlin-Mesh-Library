package no.nordicsemi.kotlin.mesh.bearer

import no.nordicsemi.kotlin.mesh.bearer.BearerError.PduTypeNotSupported


/**
 * PDU type identifies the type of the message.
 *
 * Bearers may use this type to set the proper value in the payload. For ADV bearer it will be a
 * proper AD Type (see Assigned Numbers / Generic Access Profile), for GATT bearer the correct
 * Message type in the Proxy PDU.
 *
 * Some message types are handled only by some bearers, for example the provisioning PDU type must
 * be sent using a provisioning bearer(PB type of bearer).
 *
 * @property value    PDU type value.
 * @property nonceId  The Nonce ID used for the PDU type.
 */
enum class PduType(val value: UByte) {
    NETWORK_PDU(0x00u),
    MESH_BEACON(0x01u),
    PROXY_CONFIGURATION(0x02u),
    PROVISIONING_PDU(0x03u);

    val nonceId: UByte
        get() = when(this){
            NETWORK_PDU -> 0x00u
            PROXY_CONFIGURATION -> 0x03u
            else -> throw PduTypeNotSupported
        }

    companion object {

        /**
         * Returns the PDU type from the given value.
         *
         * @param value The value of the PDU type.
         * @return PduType or throws an exception if the value is invalid.
         * @throws PduTypeNotSupported if the value is invalid.
         */
        @Throws(PduTypeNotSupported::class)
        fun from(value: UByte): PduType? = when (value) {
            0x00.toUByte() -> NETWORK_PDU
            0x01.toUByte() -> MESH_BEACON
            0x02.toUByte() -> PROXY_CONFIGURATION
            0x03.toUByte() -> PROVISIONING_PDU
            else -> null
        }
    }
}

sealed class PduTypes(val value: UByte) {

    constructor(value: Int) : this(value.toUByte())

    object NetworkPdu : PduTypes(1 shl 0)
    object MeshBeacon : PduTypes(1 shl 1)
    object ProxyConfiguration : PduTypes(1 shl 2)
    object ProvisioningPdu : PduTypes(1 shl 3)

    companion object {

        /**
         * Returns the PDU type from the given value.
         *
         * @param value The value of the PDU type.
         * @return PduType or throws [PduTypeNotSupported] if the value is invalid.
         * @throws PduTypeNotSupported if the value is invalid.
         */
        @Throws(PduTypeNotSupported::class)
        fun from(value: UByte): PduTypes = when (value) {
            0x01.toUByte() -> NetworkPdu
            0x02.toUByte() -> MeshBeacon
            0x03.toUByte() -> ProxyConfiguration
            0x04.toUByte() -> ProvisioningPdu
            else -> throw PduTypeNotSupported
        }
    }
}