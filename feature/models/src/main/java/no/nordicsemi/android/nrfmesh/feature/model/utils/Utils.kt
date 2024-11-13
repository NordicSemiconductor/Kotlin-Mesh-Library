package no.nordicsemi.android.nrfmesh.feature.model.utils

/**
 * Converts a period to time
 */
internal fun periodToTime(seconds: Int): String {
    return when (seconds) {
        1 -> "$seconds second"
        in 2..59 -> "$seconds seconds"
        in 60..3599 -> (seconds / 60).toString() + " min " + (seconds % 60) + " sec"
        in 3600..65535 ->
            (seconds / 3600).toString() + " h " + ((seconds % 3600) / 60) + " min " + (seconds % 3600 % 60) + " sec"

        else ->
            (seconds / 3600).toString() + " h " + ((seconds % 3600) / 60) + " min " + ((seconds % 3600 % 60) - 1) + " sec"
    }
}