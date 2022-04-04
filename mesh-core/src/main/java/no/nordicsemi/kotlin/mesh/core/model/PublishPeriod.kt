@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * The publish period object determines the interval at which messages are published by a model and
 * is defined by two values: the number of steps and step resolution. The publish period is
 * calculated as a product of the number of steps and step resolution. For example, if the value of
 * the numberOfSteps property is 3 and the value of the resolution property is 1000, the publish
 * period is 3000 ms.
 *
 * @property steps          Integer from 0 to 63 that represents the number of steps used to
 *                          calculate the publish period.
 * @property resolution     The resolution of the number of steps.
 * @property interval       The interval between subsequent publications in milliseconds.
 */
@Serializable
data class PublishPeriod(
    @SerialName("numberOfSteps") val steps: UByte,
    val resolution: StepResolution
) {
    val interval by lazy { resolution.toMilliseconds(steps) }

    companion object {
        /**
         * Creates the Period object with periodic publication disabled.
         */
        val disabled =
            PublishPeriod(steps = 0u, resolution = StepResolution.HUNDREDS_OF_MILLISECONDS)

        /**
         * Returns Publish Period calculated from the given duration.
         *
         * Note that this may get rounded to a different value.
         */
        fun from(interval: Duration) = interval.toPublishPeriod()
    }
}

/**
 * Converts the Duration to publish period object.
 *
 * Note that this may get rounded to a different value.
 */
fun Duration.toPublishPeriod() = PublishPeriod(
    inWholeMilliseconds.run {
        when {
            this <= 0 -> 0u
            this <= 6_300 -> (this / 100).toUByte()
            this <= 63_000 -> (this / 10_000).toUByte()
            this <= 63_000 * 60 -> (this / 60_000).toUByte()
            else -> 0x3Fu
        }
    },
    inWholeMilliseconds.run {
        when {
            this <= 0 -> StepResolution.HUNDREDS_OF_MILLISECONDS
            this <= 6_300 -> StepResolution.SECONDS
            this <= 63_000 -> StepResolution.TENS_OF_SECONDS
            this <= 63_000 * 60 -> StepResolution.TENS_OF_MINUTES
            else -> StepResolution.TENS_OF_MINUTES
        }
    }
)
