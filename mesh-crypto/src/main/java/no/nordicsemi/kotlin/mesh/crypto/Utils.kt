package no.nordicsemi.kotlin.mesh.crypto

import java.nio.ByteOrder
import java.nio.ByteOrder.nativeOrder
import java.util.*
import kotlin.experimental.xor

object Utils {
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    fun ByteArray.encodeHex(prefixOx: Boolean = false): String {
        val hex = CharArray(2 * this.size)
        this.forEachIndexed { i, byte ->
            val unsigned = 0xff and byte.toInt()
            hex[2 * i] = HEX_CHARS[unsigned / 16]
            hex[2 * i + 1] = HEX_CHARS[unsigned % 16]
        }
        return hex.joinToString("").uppercase(Locale.US).let {
            when (prefixOx) {
                true -> "0x$it"
                false -> it
            }
        }
    }

    fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { "Must have an even length" }

        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

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
    fun Int.toBigEndian(): ByteArray {
        val result = byteArrayOf(4)
        for(i in 0..3) result[i] = (this shr (24 - i*8)).toByte()

        return result
    }
}