@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.util

import java.util.*

object Utils {

    private val HEX_UUID_PATTERN = Regex("[0-9a-fA-F]{32}")

    /**
     * Converts a 4-byte array to an Int.
     *
     * @return Int.
     * @throws IllegalArgumentException If the length of byte array is not 4.
     */
    fun ByteArray.toInt(): Int {
        require(size == 4) {
            throw IndexOutOfBoundsException("byte array must be 4 bytes long")
        }
        var result = 0
        for (i in indices) {
            result = result or (this[i].toInt() shl 8 * i)
        }
        return result
    }

    /**
     * Returns an Int from a byte array with a given offset.
     *
     * @param offset The index to start from.
     * @return Int.
     * @throws IllegalArgumentException If the length of byte array is not >= offset + 4.
     */
    fun ByteArray.toInt(offset: Int): Int {
        require(size >= offset + 4) {
            throw IndexOutOfBoundsException("Cannot return an Int with the given offset")
        }
        var result = 0
        for (i in offset until offset + 4) {
            result = result or (this[i].toInt() shl 8 * i)
        }
        return result
    }

    /**
     * Converts a 2-byte array to a Short.
     *
     * @return Short.
     * @throws IllegalArgumentException If the length of byte array is not 2.
     */
    fun ByteArray.toShort(): Short {
        require(size == 2) {
            throw IndexOutOfBoundsException("byte array must be 2 bytes long")
        }
        var result = 0
        for (i in indices) {
            result = result or (this[i].toInt() shl 8 * i)
        }
        return result.toShort()
    }

    /**
     * Returns an Int from a byte array with a given offset.
     *
     * @param offset The index to start from.
     * @return Short.
     * @throws IndexOutOfBoundsException If the length of the byte array is not >= offset + 2.
     */
    fun ByteArray.toUShort(offset: Int): UShort {
        require(size >= offset + 2) {
            throw IndexOutOfBoundsException("Cannot return a Short with the given offset")
        }
        var result = 0
        for (i in offset until offset + 2) {
            result = (result shl 8) + this[i].toInt()
        }
        return result.toUShort()
    }

    fun UInt.toByteArray(data: Number, size: Int = 4): ByteArray =
        ByteArray(size) { i -> (data.toLong() shr (i * 8)).toByte() }

    /**
     * Converts an Int to a byte array.
     */
    fun UInt.toByteArray() = ByteArray(4) {
        (this shr (24 - it * 8)).toByte()
    }

    /**
     * Converts a UShort to a byte array.
     */
    fun UShort.toByteArray() = ByteArray(2) {
        (this.toInt() shr (8 - it * 8)).toByte()
    }

    /**
     * Converts a UByte to a byte array.
     */
    fun UByte.toByteArray() = ByteArray(1) {
        (this.toInt() shr (8 - it * 8)).toByte()
    }

    /**
     * Drops the dashes in the UUID.
     *
     * @return a UUID string without dashes.
     */
    fun encode(uuid: UUID) = uuid.toString().uppercase().filter { it.isLetterOrDigit() }

    /**
     * Formats a UUID string to a standard UUID format.
     */
    fun decode(uuid: String) = UUID.fromString((uuid.uppercase().takeIf {
        HEX_UUID_PATTERN.matches(it)
    }?.run {
        StringBuilder(this).apply {
            insert(8, "-")
            insert(13, "-")
            insert(18, "-")
            insert(23, "-")
        }.toString()
    } ?: uuid))
}
