package no.nordicsemi.kotlin.mesh.core.layers.lowertransport

import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey


/**
 * Lower transport layer PDU
 *
 * @property source                 Source address of the message.
 * @property destination            Destination address of the message.
 * @property networkKey             Network key used to decode/encode the PDU.
 * @property ivIndex                IV Index used to decode/encode the PDU.
 * @property type                   Message type.
 * @property transportPdu           Raw data of the lower transport layer PDU.
 * @property upperTransportPdu      Raw data of the upper transport layer PDU.
 */
internal interface LowerTransportPdu {
    val source: MeshAddress
    val destination: MeshAddress
    val networkKey: NetworkKey
    val ivIndex: UInt
    val type: LowerTransportPduType
    val transportPdu: ByteArray
    val upperTransportPdu: ByteArray
}

/**
 * Defines the Lower Transport PDU types.
 */
internal enum class LowerTransportPduType(val rawValue: Byte) {
    ACCESS_MESSAGE(rawValue = 0),
    CONTROL_MESSAGE(rawValue = 1);

    val netMicSize: Int
        get() = when (this) {
            ACCESS_MESSAGE -> 4  // 32 bits
            CONTROL_MESSAGE -> 8 // 64 bits
        }

    companion object {

        /**
         * Initializes the Lower Transport pdu type from the given value.
         *
         * @param type Type of the Lower Transport PDU.
         * @return LowerTransportPduType or null if the values don't match.
         */
        fun from(type: Byte) = entries.firstOrNull { it.rawValue == type }
    }
}
