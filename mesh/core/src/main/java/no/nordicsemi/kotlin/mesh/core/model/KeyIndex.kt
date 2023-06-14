package no.nordicsemi.kotlin.mesh.core.model

/**
 * A Key Index is 12-bit long Unsigned Integer.
 * This property returns `true` if the value is in range 0...4095.
 */
typealias KeyIndex = UShort

/**
 * Checks if the Key index is of the given valid range.
 *
 * @return true if valid or false otherwise.
 */
fun KeyIndex.isValidKeyIndex() = this.toInt() in 0..4095