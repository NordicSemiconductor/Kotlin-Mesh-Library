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
     * @param a First byte array.
     * @param b Second byte array.
     * @return XOR of the two byte arrays.
     *
     */
    fun xor(a: ByteArray, b: ByteArray): ByteArray {
        val result = ByteArray(a.count())
        for (i in a.indices) {
            result[i] = (a[i] xor b[i % b.count()])
        }
        return result
    }

    /**
     * Given an integer, function computes the big endian representation of it.
     *
     * @param i Integer value.
     * @return Byte Array - Big endian representation of the integer.
     *
     */
    fun intToBigEndian(i: Int): ByteArray {
        val result = byteArrayOf(i.toByte())
        if (nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            result.reverse()
        }
        return result
    }
}