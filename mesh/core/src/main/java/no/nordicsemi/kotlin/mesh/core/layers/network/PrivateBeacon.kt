@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.core.model.IvIndex
import no.nordicsemi.kotlin.mesh.core.model.KeyDistribution
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey
import no.nordicsemi.kotlin.mesh.core.util.Utils.toInt
import no.nordicsemi.kotlin.mesh.crypto.Crypto
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex

/**
 * Defines Private Beacon transmitted by the mesh network.
 */
internal data class PrivateBeacon(
    override val pdu: ByteArray,
    override val networkKey: NetworkKey,
    override val validForKeyRefreshProcedure: Boolean,
    override val keyRefreshFlag: Boolean,
    override val ivIndex: IvIndex
) : NetworkBeaconPdu {
    override val beaconType = BeaconType.PRIVATE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PrivateBeacon

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

    override fun toString() = "Secure Network beacon (" +
            "Network ID: ${networkKey.networkId?.encodeHex(prefixOx = true)}, " +
            "IV Index: $ivIndex, " +
            "Key Refresh Flag: $keyRefreshFlag)"
}

internal object PrivateBeaconDecoder {

    /**
     * Decodes the private beacon
     *
     * @param pdu          Private beacon pdu.
     * @param networkKey   Network key to decode with.
     * @return PrivateBeacon or null if the beacon could not be decoded or authenticated.
     */
    fun decode(pdu: ByteArray, networkKey: NetworkKey): PrivateBeacon? {
        require(pdu.size == 27 && pdu[0].toInt() == 2) { return null }

        val privateBeaconData = Crypto.decodeAndAuthenticate(
            pdu = pdu, privateBeaconKey = networkKey.derivatives.privateBeaconKey
        )

        return if (privateBeaconData == null &&
            networkKey.phase == KeyDistribution &&
            networkKey.oldDerivatives != null
        ) {
            Crypto.decodeAndAuthenticate(
                pdu = pdu, privateBeaconKey = networkKey.oldDerivatives!!.privateBeaconKey
            )?.let {
                val flags = it.first
                val keyRefreshFlag = (flags.toInt() and 0x01) != 0
                val updateActive = (flags.toInt() and 0x02) != 0
                val index = it.second.toInt(offset = 0)
                val ivIndex = IvIndex(index = index.toUInt(), isIvUpdateActive = updateActive)
                PrivateBeacon(
                    pdu = pdu,
                    networkKey = networkKey,
                    validForKeyRefreshProcedure = true,
                    keyRefreshFlag = keyRefreshFlag,
                    ivIndex = ivIndex
                )
            }
        } else null
    }
}