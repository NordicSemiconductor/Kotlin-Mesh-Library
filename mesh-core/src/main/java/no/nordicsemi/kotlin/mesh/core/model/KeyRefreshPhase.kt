package no.nordicsemi.kotlin.mesh.core.model

/**
 * The Key Refresh Phase state indicates and controls the Key Refresh procedure for each NetKey in the NetKey List.
 */
@Suppress("unused")
enum class KeyRefreshPhase(val phase: Int) {

    // TODO to be verified when Key refresh support is added.
    /** Normal operation; Key Refresh procedure is not active **/
    NORMAL_OPERATION(phase = 0),

    /** First phase of Key Refresh procedure **/
    KEY_DISTRIBUTION(phase = 1),

    /** Second phase of Key Refresh procedure **/
    USING_NEW_KEYS(phase = 2);

    companion object {

        /**
         * Parses the phase of the key refresh procedure.
         * @param phase Key refresh procedure phase.
         * @return [KeyRefreshPhase]
         * @throws IllegalArgumentException  if the phase level value is not 0, 1 or 2.
         */
        @Suppress("unused")
        fun from(phase: Int): KeyRefreshPhase = when (phase) {
            0 -> NORMAL_OPERATION
            1 -> KEY_DISTRIBUTION
            2 -> USING_NEW_KEYS
            else -> throw IllegalArgumentException("Invalid value, phase must be an integer of value 0, 1 or 2")
        }
    }
}