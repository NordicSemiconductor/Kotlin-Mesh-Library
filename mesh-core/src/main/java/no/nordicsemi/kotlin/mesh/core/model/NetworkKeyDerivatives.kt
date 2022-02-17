package no.nordicsemi.kotlin.mesh.core.model

/**
 * Network Key Derivatives
 *
 * @param nid               Network identifier
 * @param networkId         Network ID
 * @param encryptionKey     Encryption Key
 * @param privacyKey        Privacy Key
 * @param identityKey       Identity Key
 * @param beaconKey         Beacon Key
 */
data class NetworkKeyDerivatives(
    val nid: Int,
    val networkId: ByteArray,
    val encryptionKey: ByteArray,
    val privacyKey: ByteArray,
    val identityKey: ByteArray,
    val beaconKey: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NetworkKeyDerivatives

        if (nid != other.nid) return false
        if (!networkId.contentEquals(other.networkId)) return false
        if (!encryptionKey.contentEquals(other.encryptionKey)) return false
        if (!privacyKey.contentEquals(other.privacyKey)) return false
        if (!identityKey.contentEquals(other.identityKey)) return false
        if (!beaconKey.contentEquals(other.beaconKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = nid
        result = 31 * result + networkId.contentHashCode()
        result = 31 * result + encryptionKey.contentHashCode()
        result = 31 * result + privacyKey.contentHashCode()
        result = 31 * result + identityKey.contentHashCode()
        result = 31 * result + beaconKey.contentHashCode()
        return result
    }
}