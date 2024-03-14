package no.nordicsemi.kotlin.mesh.crypto

import org.bouncycastle.jce.interfaces.ECPublicKey
import java.nio.charset.StandardCharsets
import java.security.PublicKey

/**
 * Returns the public key encoded as a 64-byte array
 *
 * @receiver Public key.
 */
fun PublicKey.toByteArray() = (this as ECPublicKey).q.getEncoded(false).let { key ->
    // Drop the first byte that contains the encoding.
    key.copyOfRange(1, key.size)
}

// TODO: Move to Mesh Sniffer or anywhere else

/**
 * Function converts a byte array encoded in uint16 to utf8. Each byte is encoded in utf8 as follows:
 * * 0xxxxxxx - 1 byte (0xxxxxxx in uint16)
 * * 110yyyyy 10xxxxxx - 2 bytes (00000yyy yyxxxxxx in uint16)
 * * 1110zzzz 10yyyyyy 10xxxxxx - 3 bytes (zzzzyyyy yyxxxxxx in uint16)
 * @return Byte Array - utf8 representation of the uint16 array.
 */
fun ByteArray.uint16ToUtf8(): ByteArray {
    require(this.size % 2 == 0)
    val charArray = CharArray(this.size / 2)
    for (i in this.indices step 2) {
        val uint16 = ((this[i].toInt() and 0xff) shl 8) or (this[i + 1].toInt() and 0xff)
        charArray[i / 2] = uint16.toChar()
    }
    return String(charArray).toByteArray(StandardCharsets.UTF_8)
}

/**
 * Function converts a byte array encoded in utf8 to uint16. Each byte is encoded in utf8 as follows:
 * * 1 byte (0xxxxxxx in uint16) - 0xxxxxxx
 * * 2 bytes (00000yyy yyxxxxxx in uint16) - 110yyyyy 10xxxxxx
 * * 3 bytes (zzzzyyyy yyxxxxxx in uint16) - 1110zzzz 10yyyyyy 10xxxxxx
 * @return Byte Array - uint16 representation of the utf8 array.
 */
fun ByteArray.utf8ToUint16(): ByteArray {
    val charArray = String(this, StandardCharsets.UTF_8).toCharArray()
    val output = ByteArray(charArray.size * 2)
    for (i in charArray.indices) {
        val uint16 = charArray[i].code
        output[i * 2] = (uint16 shr 8).toByte()
        output[i * 2 + 1] = uint16.toByte()
    }
    return output
}