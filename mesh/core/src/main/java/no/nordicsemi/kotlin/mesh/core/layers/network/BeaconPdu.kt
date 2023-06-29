package no.nordicsemi.kotlin.mesh.core.layers.network

/**
 * Enum defining the different types of beacons.
 */
internal enum class BeaconType(val type: UByte) {
    UNPROVISIONED_DEVICE(0u),
    SECURE_NETWORK(1u),
    PRIVATE(2u);

    companion object {
        fun from(type: UByte): BeaconType? = values().firstOrNull { it.type == type }
    }
}

/**
 * Base interface defining a Beacon PDU.
 *
 * @property pdu         Raw beacon PDU data.
 * @property beaconType  Type of beacon.
 */
internal sealed interface BeaconPdu {
    val pdu: ByteArray
    val beaconType: BeaconType
}