@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.KeyDistribution
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.util.Utils.toInt
import no.nordicsemi.kotlin.mesh.crypto.Crypto

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

internal object SecureNetworkBeaconDecoder {

    fun decode(pdu: ByteArray, networkKey: NetworkKey): SecureNetworkBeacon? {

        require(pdu.size == 22 && pdu[0].toInt() == 1) { return null }

        val keyRefreshFlag = pdu[1].toInt() and 0x01 != 0
        val updateActive = pdu[1].toInt() and 0x02 != 0
        val networkId = pdu.sliceArray(2 until 10)
        val index = pdu.toInt(offset = 10)
        val ivIndex = IvIndex(index.toUInt(), updateActive)
        val validForKeyRefreshProcedure = networkKey.oldKey != null

        when {
            networkId.contentEquals(networkKey.networkId) -> {
                require(Crypto.authenticate(pdu, networkKey.derivatives.beaconKey)) {
                    return null
                }
                return SecureNetworkBeacon(
                    pdu = pdu,
                    networkKey = networkKey,
                    validForKeyRefreshProcedure = validForKeyRefreshProcedure,
                    keyRefreshFlag = keyRefreshFlag,
                    ivIndex = ivIndex
                )
            }

            networkKey.phase == KeyDistribution &&
                    networkId.contentEquals(networkKey.oldNetworkId) &&
                    networkKey.oldKey != null -> {
                require(Crypto.authenticate(pdu, networkKey.derivatives.beaconKey)) {
                    return null
                }
                return SecureNetworkBeacon(
                    pdu = pdu,
                    networkKey = networkKey,
                    validForKeyRefreshProcedure = validForKeyRefreshProcedure,
                    keyRefreshFlag = keyRefreshFlag,
                    ivIndex = ivIndex
                )
            }

            else -> return null
        }
    }
}