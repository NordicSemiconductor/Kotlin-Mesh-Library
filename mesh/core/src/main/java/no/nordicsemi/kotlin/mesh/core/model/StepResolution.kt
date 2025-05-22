package no.nordicsemi.kotlin.mesh.core.model

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Mesh implementation of step resolution is a 2-bit field that enumerates the number of steps.
 *
 * @param value Step resolution.
 */
enum class StepResolution(val value: UByte) {
    HUNDREDS_OF_MILLISECONDS(0b00u),
    SECONDS(0b01u),
    TENS_OF_SECONDS(0b10u),
    TENS_OF_MINUTES(0b11u);

    fun toMilliseconds(steps: UByte): Duration = when (this) {
        HUNDREDS_OF_MILLISECONDS -> (steps.toInt() * 100).toDuration(DurationUnit.MILLISECONDS)
        SECONDS -> steps.toInt().toDuration(DurationUnit.SECONDS)
        TENS_OF_SECONDS -> (steps.toInt() * 10).toDuration(DurationUnit.SECONDS)
        TENS_OF_MINUTES -> (steps.toInt() * 10).toDuration(DurationUnit.MINUTES)
    }

    internal fun toResolution() = when (this) {
        HUNDREDS_OF_MILLISECONDS -> 100
        SECONDS -> 1000
        TENS_OF_SECONDS -> 10000
        TENS_OF_MINUTES -> 600000
    }

    companion object {
        // TODO: change to Duration???
        fun from(resolution: Int): StepResolution = when (resolution) {
            100 -> HUNDREDS_OF_MILLISECONDS
            1000 -> SECONDS
            10000 -> TENS_OF_SECONDS
            600000 -> TENS_OF_MINUTES
            else -> throw IllegalArgumentException("Invalid resolution: $resolution")
        }

        /**
         * Returns the StepResolution for the given number of steps.
         */
        @Throws(IllegalArgumentException::class)
        fun from(value: UByte): StepResolution = entries.first { it.value == value }
    }
}