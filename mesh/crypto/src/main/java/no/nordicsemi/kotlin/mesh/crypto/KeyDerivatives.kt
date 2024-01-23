package no.nordicsemi.kotlin.mesh.crypto

/**
 * Derivatives of a Mesh Network Key
 *
 * @param nid               Network Identifier
 * @param encryptionKey     Encryption key for a given network key
 * @param privacyKey        PrivacyKey for a given NetworkKey
 * @param networkId         64-bit NetworkID used to differentiate networks derived of network key
 * @param identityKey       IdentityKey for a given NetworkKey
 * @param beaconKey         Beacon key for a given NetworkKey
 * @param privateBeaconKey  Private Beacon key for a given NetworkKey
 */
data class KeyDerivatives internal constructor(
    val nid: Byte,
    val encryptionKey: ByteArray,
    val privacyKey: ByteArray,
    val networkId: ByteArray,
    val identityKey: ByteArray,
    val beaconKey: ByteArray,
    val privateBeaconKey: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyDerivatives

        if (nid != other.nid) return false
        if (!encryptionKey.contentEquals(other.encryptionKey)) return false
        if (!privacyKey.contentEquals(other.privacyKey)) return false
        if (!networkId.contentEquals(other.networkId)) return false
        if (!identityKey.contentEquals(other.identityKey)) return false
        if (!beaconKey.contentEquals(other.beaconKey)) return false
        return privateBeaconKey.contentEquals(other.privateBeaconKey)
    }

    override fun hashCode(): Int {
        var result = nid.hashCode()
        result = 31 * result + encryptionKey.contentHashCode()
        result = 31 * result + privacyKey.contentHashCode()
        result = 31 * result + networkId.contentHashCode()
        result = 31 * result + identityKey.contentHashCode()
        result = 31 * result + beaconKey.contentHashCode()
        result = 31 * result + privateBeaconKey.contentHashCode()
        return result
    }

}
