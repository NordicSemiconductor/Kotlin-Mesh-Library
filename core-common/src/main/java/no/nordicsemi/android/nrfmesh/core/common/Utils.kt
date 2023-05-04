@file:Suppress("unused")

package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.logger.LogLevel

/**
 * Helper object containing utility methods.
 */
object Utils {

    /**
     * Converts the [LogLevel] to an Android log level.
     *
     * @receiver LogLevel
     * @return the Android log level
     */
    fun LogLevel.toAndroidLogLevel(): Int = when (this) {
        LogLevel.VERBOSE -> android.util.Log.VERBOSE
        LogLevel.DEBUG -> android.util.Log.DEBUG
        LogLevel.INFO -> android.util.Log.INFO
        LogLevel.APPLICATION -> android.util.Log.INFO
        LogLevel.WARNING -> android.util.Log.WARN
        LogLevel.ERROR -> android.util.Log.ERROR
    }
}