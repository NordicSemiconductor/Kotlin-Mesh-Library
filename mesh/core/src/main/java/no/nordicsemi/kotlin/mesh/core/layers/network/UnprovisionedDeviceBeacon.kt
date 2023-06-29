@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.layers.network

import no.nordicsemi.kotlin.mesh.core.oob.OobInformation
import no.nordicsemi.kotlin.mesh.core.util.Utils
import no.nordicsemi.kotlin.mesh.crypto.Utils.encodeHex
import java.util.UUID

/**
 * Class that defines an Unprovisioned device beacon PDU.
 *
 * @property pdu               Raw beacon PDU data.
 * @property deviceUuid        UUID of the unprovisioned device.
 * @property oobInformation    OOB information.
 * @property uriHash           URI hash.
 * @property beaconType        Type of beacon.
 */
internal data class UnprovisionedDeviceBeacon(
    override val pdu: ByteArray,
    val deviceUuid: UUID,
    val oobInformation: OobInformation,
    val uriHash: ByteArray? = null
) : BeaconPdu {

    override val beaconType: BeaconType = BeaconType.UNPROVISIONED_DEVICE

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UnprovisionedDeviceBeacon

        if (!pdu.contentEquals(other.pdu)) return false
        if (deviceUuid != other.deviceUuid) return false
        if (oobInformation != other.oobInformation) return false
        if (uriHash != null) {
            if (other.uriHash == null) return false
            if (!uriHash.contentEquals(other.uriHash)) return false
        } else if (other.uriHash != null) return false
        if (beaconType != other.beaconType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pdu.contentHashCode()
        result = 31 * result + deviceUuid.hashCode()
        result = 31 * result + oobInformation.hashCode()
        result = 31 * result + (uriHash?.contentHashCode() ?: 0)
        result = 31 * result + beaconType.hashCode()
        return result
    }

    override fun toString() = "Unprovisioned Device beacon (UUID: $deviceUuid \n," +
            "OOB Information: $oobInformation \n, URI Hash: ${uriHash?.encodeHex()}"
}

internal object UnprovisionedDeviceBeaconDecoder {

    /**
     * Decodes the unprovisioned device beacon.
     *
     * @param pdu Unprovisioned device beacon PDU.
     * @return an Unprovisioned Device beacon or null otherwise.
     */
    fun decode(pdu: ByteArray): UnprovisionedDeviceBeacon? = when {
        pdu.size > 1 -> when (BeaconType.from(pdu[0].toUByte())) {
            BeaconType.UNPROVISIONED_DEVICE -> {
                val uuid = Utils.decode(pdu.sliceArray(1 until 17).encodeHex())
                val oob = OobInformation.from(pdu[17].toUShort())
                val uriHash = when (pdu.size == 23) {
                    true -> pdu.sliceArray(19 until pdu.size)
                    false -> null
                }
                UnprovisionedDeviceBeacon(pdu, uuid, oob, uriHash)
            }

            else -> null
        }

        else -> null
    }
}
