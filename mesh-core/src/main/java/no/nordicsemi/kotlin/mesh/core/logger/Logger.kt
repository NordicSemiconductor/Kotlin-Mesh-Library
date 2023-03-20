@file:Suppress("unused")

package no.nordicsemi.kotlin.mesh.core.logger

interface Logger {

    /**
     * Invoked whenever a new log entry is to be saved.
     * Important: It is NOT safe to update the UI from this method as multiple threads may log.
     *
     * @param message  Message.
     * @param category Message category.
     * @param level    Log level.
     * */
    fun log(message: String, category: LogCategory, level: LogLevel)

    fun d(category: LogCategory, message: () -> String) {
        log(message(), category, LogLevel.DEBUG)
    }

    fun v(category: LogCategory, message: () -> String) {
        log(message(), category, LogLevel.VERBOSE)
    }

    fun i(category: LogCategory, message: () -> String) {
        log(message(), category, LogLevel.INFO)
    }

    fun a(category: LogCategory, message: () -> String) {
        log(message(), category, LogLevel.APPLICATION)
    }

    fun w(category: LogCategory, message: () -> String) {
        log(message(), category, LogLevel.WARNING)
    }

    fun w(category: LogCategory, throwable: Throwable) {
        log(throwable.toString(), category, LogLevel.WARNING)
    }

    fun e(category: LogCategory, message: () -> String) {
        log(message(), category, LogLevel.ERROR)
    }

    fun e(category: LogCategory, throwable: Throwable) {
        log(throwable.toString(), category, LogLevel.ERROR)
    }
}