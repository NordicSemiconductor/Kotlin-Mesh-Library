package no.nordicsemi.kotlin.mesh.core.model

/**
 * The Key Refresh Phase state indicates and controls the Key Refresh procedure for each NetKey in the NetKey List.
 */
@Suppress("unused")
sealed class KeyRefreshPhase(val phase: Int) {
    // TODO to be verified when Key refresh support is added.
    companion object {
        /**
         * Parses the phase of the key refresh procedure.
         *
         * @param phase Key refresh procedure phase.
         * @return [KeyRefreshPhase]
         * @throws IllegalArgumentException  if the phase level value is not 0, 1 or 2.
         */
        @Suppress("unused")
        fun from(phase: Int): KeyRefreshPhase = when (phase) {
            0 -> NormalOperation
            1 -> KeyDistribution
            2 -> UsingNewKeys
            else -> throw IllegalArgumentException("Invalid value, phase must be an integer of value 0, 1 or 2!")
        }
    }
}

/**
 * Normal operation; Key Refresh procedure is not active.
 */
object NormalOperation : KeyRefreshPhase(phase = NORMAL_OPERATION)

/**
 * First phase of Key Refresh procedure.
 */
object KeyDistribution : KeyRefreshPhase(phase = KEY_DISTRIBUTION)

/**
 * Second phase of Key Refresh procedure.
 */
object UsingNewKeys : KeyRefreshPhase(phase = USING_NEW_KEYS)

const val NORMAL_OPERATION = 0
const val KEY_DISTRIBUTION = 1
const val USING_NEW_KEYS = 2