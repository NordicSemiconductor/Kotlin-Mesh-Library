package no.nordicsemi.kotlin.mesh.core.util

import no.nordicsemi.kotlin.mesh.core.model.TransitionTime
import no.nordicsemi.kotlin.mesh.core.messages.generic.GenericOnOffSet
import no.nordicsemi.kotlin.mesh.core.messages.generic.GenericOnOffSetUnacknowledged

/**
 * Defines Transition parameters to be used by [GenericOnOffSet], [GenericOnOffSetUnacknowledged]
 *
 * @property transitionTime Defines the time interval an element would take to transition from one
 *                          state to another.
 * @property delay          Defines the time interval before the transition begins.
 */
data class TransitionParameters(val transitionTime: TransitionTime, val delay: UByte)