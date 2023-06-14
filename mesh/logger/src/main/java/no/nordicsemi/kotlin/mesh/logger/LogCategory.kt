@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package no.nordicsemi.kotlin.mesh.logger

/**
 * The log category indicates the component that created the log entry.
 *
 * @property category Category name.
 */
enum class LogCategory(val category: String) {
    /**
     * Log created by the Bearer component.
     */
    BEARER("Bearer"),

    /**
     * Log created by the Proxy component.
     */
    PROXY("Proxy"),

    /**
     * Log created by the Network component.
     */
    NETWORK("Network"),

    /**
     * Log created by the Lower Transport component.
     */
    LOWER_TRANSPORT("LowerTransport"),

    /**
     * Log created by the Upper Transport component.
     */
    UPPER_TRANSPORT("UpperTransport"),

    /**
     * Log created by the Access component.
     */
    ACCESS("Access"),

    /**
     * Log created by the FoundationModel component.
     */
    FOUNDATION_MODEL("FoundationModel"),

    /**
     * Log created by the Model component.
     */
    MODEL("Model"),

    /**
     * Log created by the Provisioning component.
     */
    PROVISIONING("Provisioning"),
}