@file:OptIn(ExperimentalStdlibApi::class)

package no.nordicsemi.kotlin.mesh.crypto

import java.nio.charset.StandardCharsets
import kotlin.experimental.xor

object Utils {

    /**
     * Converts a byte array to a hex string.
     *
     * @param prefixOx Whether to prefix the hex string with 0x.
     * @return Hex string representation of the byte array.
     */
    fun ByteArray.encodeHex(prefixOx: Boolean = false) = (if(prefixOx) "0x" else "") + toHexString().uppercase()

    /**
     * Converts a byte array to a hex string.
     */
    fun String.decodeHex() = hexToByteArray()

    /**
     * Applies the XOR operator on two byte arrays. Compared to already existent
     * xor functions, this one does not require the arrays to be of the same length.
     *
     * @param other The other byte array which is xor ed with this one.
     * @return XOR of the two byte arrays.
     */
    infix fun ByteArray.xor(other: ByteArray): ByteArray {
        val result = ByteArray(this.count())
        for (i in this.indices) {
            result[i] = (this[i] xor other[i % other.count()])
        }
        return result
    }

    /**
     * Function converts an integer to Big Endian representation.
     *
     * @return Byte Array - Big endian representation of the integer.
     */
    fun UInt.toBigEndian(): ByteArray {
        val result = byteArrayOf(0, 0, 0, 0)
        for(i in 0..3) result[i] = (this shr (24 - i*8)).toByte()
        return result
    }

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
}