package no.nordicsemi.kotlin.mesh.core.model

import kotlinx.serialization.SerialName

/**
 * The publish period object determines the interval at which messages are published by a model and is defined
 * by two values: the number of steps and step resolution. The publish period is calculated as a product of the
 * number of steps and step resolution. For example, if the value of the numberOfSteps property is 3 and the
 * value of the resolution property is 1000, the publish period is 3000 ms.
 *
 * @param steps         Integer from 0 to 63 that represents the number of steps used to calculate the publish period.
 * @param resolution    The resolution property contains an integer that represents the publish step resolution in milliseconds.
                        The allowed values are: 100, 1000, 10000, and 600000.
 */
data class PublishPeriod(@SerialName("numberOfSteps") val steps: Int, val resolution: Int)
