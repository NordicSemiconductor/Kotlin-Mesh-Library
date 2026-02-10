package no.nordicsemi.android.nrfmesh.ui.network

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.DashboardCustomize
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.HourglassEmpty
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.VpnKey

sealed class Configuration(
    val networkKeys: Int = 0,
    val applicationKeys: Int = 0,
    val groups: Int = 0,
    val virtualGroups: Int = 0,
    val scenes: Int = 0,
) {
    data object Empty : Configuration(networkKeys = 1)

    class  Custom(
        networkKeys: Int = 1,
        applicationKeys: Int = 0,
        groups: Int = 3,
        virtualGroups: Int = 1,
        scenes: Int = 4,
    ) : Configuration(
        networkKeys = networkKeys,
        applicationKeys = applicationKeys,
        groups = groups,
        virtualGroups = virtualGroups,
        scenes = scenes
    )

    class Debug(
        networkKeys: Int = 1,
        applicationKeys: Int = 1,
        groups: Int = 3,
        virtualGroups: Int = 1,
        scenes: Int = 4,
    ) : Configuration(
        networkKeys = networkKeys,
        applicationKeys = applicationKeys,
        groups = groups,
        virtualGroups = virtualGroups,
        scenes = scenes
    )

    data object Import : Configuration()
}

val configurations =
    listOf(Configuration.Empty, Configuration.Custom(), Configuration.Debug(), Configuration.Import)

internal fun Configuration.icon() = when (this) {
    is Configuration.Empty -> Icons.Outlined.HourglassEmpty
    is Configuration.Custom -> Icons.Outlined.DashboardCustomize
    is Configuration.Debug -> Icons.Outlined.BugReport
    is Configuration.Import -> Icons.Outlined.ImportExport
}

internal fun Configuration.description(): String {
    return when (this) {
        is Configuration.Empty -> "Empty"
        is Configuration.Custom -> "Custom"
        is Configuration.Debug -> "Debug"
        is Configuration.Import -> "Import"
    }
}

enum class ConfigurationProperty {
    NETWORK_KEYS,
    APPLICATION_KEYS,
    GROUPS,
    VIRTUAL_GROUPS,
    SCENES
}

fun ConfigurationProperty.description(): String = when (this) {
    ConfigurationProperty.NETWORK_KEYS -> "Network Keys"
    ConfigurationProperty.APPLICATION_KEYS -> "Application Keys"
    ConfigurationProperty.GROUPS -> "Groups"
    ConfigurationProperty.VIRTUAL_GROUPS -> "Virtual Groups"
    ConfigurationProperty.SCENES -> "Scenes"
}

fun ConfigurationProperty.icon() = when (this) {
    ConfigurationProperty.NETWORK_KEYS,
    ConfigurationProperty.APPLICATION_KEYS,
        -> Icons.Outlined.VpnKey

    ConfigurationProperty.GROUPS,
    ConfigurationProperty.VIRTUAL_GROUPS,
        -> Icons.Outlined.GroupWork

    ConfigurationProperty.SCENES -> Icons.Outlined.AutoAwesome
}

enum class Action{
    ADD,
    REMOVE
}