package no.nordicsemi.kotlin.mesh.crypto

/**
 * Security material for a given network key.
 */
internal data class SecurityMaterial(
    val nid: Byte,
    val encryptionKey: ByteArray,
    val privacyKey: ByteArray,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SecurityMaterial

        if (nid != other.nid) return false
        if (!encryptionKey.contentEquals(other.encryptionKey)) return false
        return privacyKey.contentEquals(other.privacyKey)
    }

    override fun hashCode(): Int {
        var result = nid.toInt()
        result = 31 * result + encryptionKey.contentHashCode()
        result = 31 * result + privacyKey.contentHashCode()
        return result
    }
}