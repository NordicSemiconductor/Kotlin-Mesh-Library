package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class NodeKey(val nodeUuid: String) : NavKey

/**
 * Defines a unique scene key that identifies a list and detail pane belonging to
 * NodeListDetailScene. This is required when using a single NavDisplay with multiple
 * ListDetailPanes
 */
@Serializable
data object NodeListDetailSceneKey

/**
 * Defines a unique scene key that identifies a list and detail pane belonging to
 * GroupsListDetailScene. This is required when using a single NavDisplay with multiple
 * ListDetailPanes
 */
@Serializable
data object GroupsListDetailSceneKey

/**
 * Defines a unique scene key that identifies a list and detail pane belonging to
 * SettingsListDetailScene. This is required when using a single NavDisplay with multiple
 * ListDetailPanes
 */
@Serializable
data object SettingsListDetailSceneKey