@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

/**
 * Defines Secure Network Beacon transmitted by the mesh network.
 */
internal data class SecureNetworkBeacon(
    override val pdu: ByteArray,
    override val networkKey: NetworkKey,
    override val validForKeyRefreshProcedure: Boolean,
    override val keyRefreshFlag: Boolean,
    override val ivIndex: IvIndex
) : NetworkBeaconPdu {

    override val beaconType: BeaconType = BeaconType.SECURE_NETWORK

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecureNetworkBeacon

        if (!pdu.contentEquals(other.pdu)) return false
        if (networkKey != other.networkKey) return false
        if (validForKeyRefreshProcedure != other.validForKeyRefreshProcedure) return false
        if (keyRefreshFlag != other.keyRefreshFlag) return false
        if (ivIndex != other.ivIndex) return false
        if (beaconType != other.beaconType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pdu.contentHashCode()
        result = 31 * result + networkKey.hashCode()
        result = 31 * result + validForKeyRefreshProcedure.hashCode()
        result = 31 * result + keyRefreshFlag.hashCode()
        result = 31 * result + ivIndex.hashCode()
        result = 31 * result + beaconType.hashCode()
        return result
    }
}