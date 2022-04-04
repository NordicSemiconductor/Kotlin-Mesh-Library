package no.nordicsemi.kotlin.mesh.core.model

/**
 * A Key Index is 12-bit long Unsigned Integer.
 * This property returns `true` if the value is in range 0...4095.
 */
typealias KeyIndex = UShort

fun KeyIndex.isValidKeyIndex() = this.toInt() in 0..4095