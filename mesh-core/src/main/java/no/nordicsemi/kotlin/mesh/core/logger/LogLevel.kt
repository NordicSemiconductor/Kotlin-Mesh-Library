@file:Suppress("MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.core.logger


/**
 * Log level, which allows filtering logs by importance.
 *
 * @property level     Log leve.
 * @property levelName Shortened abbreviation of the log level.
 */
enum class LogLevel(val level: Int) {
    /**
     * Lowest priority. Usually names of called methods or callbacks received.
     */
    DEBUG(0),

    /**
     *  Low priority messages what the service is doing.
     */
    VERBOSE(1),

    /**
     * Messages about completed tasks.
     */
    INFO(5),

    /**
     * Messages about application level events, in this case DFU messages in human-readable form.
     */
    APPLICATION(10),

    /**
     * Important messages.
     */
    WARNING(15),

    /**
     * Highest priority messages with errors.
     */
    ERROR(20);

    val levelName: String
        get() = when (this) {
            DEBUG -> "D"
            VERBOSE -> "V"
            INFO -> "I"
            APPLICATION -> "A"
            WARNING -> "W"
            ERROR -> "E"
        }
}