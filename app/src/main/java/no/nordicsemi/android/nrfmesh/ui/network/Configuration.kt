package no.nordicsemi.android.nrfmesh.ui.network

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.VpnKey

/**
 * Network properties
 *
 * @property networkKeys     Number of network keys.
 * @property applicationKeys Number of application keys.
 * @property groups          Number of groups.
 * @property virtualGroups   Number of virtual groups.
 * @property scenes          Number of scenes.
 */
interface NetworkProperties{
    val networkKeys: Int?
    val applicationKeys: Int?
    val groups: Int?
    val virtualGroups: Int?
    val scenes: Int?
}

/**
 * Defines the network configuration used by the Network Wizard
 */
sealed class Configuration {

    /**
     * Empty configuration.
     */
    data object Empty : Configuration(), NetworkProperties {
        override val networkKeys = 1
        override val applicationKeys = 0
        override val groups = 0
        override val virtualGroups = 0
        override val scenes = 0
    }

    /**
     * Custom configuration.
     */
    data class  Custom(
        override val networkKeys: Int = 1,
        override val applicationKeys: Int = 1,
        override val groups: Int = 3,
        override val virtualGroups: Int = 1,
        override val scenes: Int = 4,
    ) : Configuration(), NetworkProperties

    /**
     * Debug configuration.
     */
    data class Debug(
        override val networkKeys: Int = 1,
        override val applicationKeys: Int = 0,
        override val groups: Int = 3,
        override val virtualGroups: Int = 1,
        override val scenes: Int = 4,
    ) : Configuration(), NetworkProperties

    /**
     * Import configuration.
     */
    data object Import : Configuration(), NetworkProperties {
        override val networkKeys = null
        override val applicationKeys = null
        override val groups = null
        override val virtualGroups = null
        override val scenes = null
    }
}


/**
 * Returns the icon for the configuration.
 */
internal fun Configuration.icon() = when (this) {
    is Configuration.Empty -> Icons.Outlined.HourglassEmpty
    is Configuration.Custom -> Icons.Outlined.DashboardCustomize
    is Configuration.Debug -> Icons.Outlined.BugReport
    is Configuration.Import -> Icons.Outlined.ImportExport
}

/**
 * Returns the description for the configuration.
 */
internal fun Configuration.description(): String {
    return when (this) {
        is Configuration.Empty -> "Empty"
        is Configuration.Custom -> "Custom"
        is Configuration.Debug -> "Debug"
        is Configuration.Import -> "Import"
    }
}

/**
 * Configuration property.
 */
enum class ConfigurationProperty {
    NETWORK_KEYS,
    APPLICATION_KEYS,
    GROUPS,
    VIRTUAL_GROUPS,
    SCENES
}

/**
 * Returns the description for the configuration property.
 */
fun ConfigurationProperty.description(): String = when (this) {
    ConfigurationProperty.NETWORK_KEYS -> "Network Keys"
    ConfigurationProperty.APPLICATION_KEYS -> "Application Keys"
    ConfigurationProperty.GROUPS -> "Groups"
    ConfigurationProperty.VIRTUAL_GROUPS -> "Virtual Groups"
    ConfigurationProperty.SCENES -> "Scenes"
}

/**
 * Returns the icon for the configuration property.
 */
fun ConfigurationProperty.icon() = when (this) {
    ConfigurationProperty.NETWORK_KEYS,
    ConfigurationProperty.APPLICATION_KEYS,
        -> Icons.Outlined.VpnKey

    ConfigurationProperty.GROUPS,
    ConfigurationProperty.VIRTUAL_GROUPS,
        -> Icons.Outlined.GroupWork

    ConfigurationProperty.SCENES -> Icons.Outlined.AutoAwesome
}

/**
 * Action
 */
enum class Action{
    /**
     * Add action.
     */
    ADD,

    /**
     * Remove action.
     */
    REMOVE
}