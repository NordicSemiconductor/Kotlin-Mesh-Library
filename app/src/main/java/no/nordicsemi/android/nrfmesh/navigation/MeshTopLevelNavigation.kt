/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package no.nordicsemi.android.nrfmesh.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import no.nordicsemi.android.nrfmesh.core.navigation.R
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsDestination
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesDestination
import no.nordicsemi.android.nrfmesh.feature.proxyfilter.navigation.ProxyFilterDestination
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsDestination

/**
 * Routes for the different top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composables.
 */

/**
 * Models the navigation top level actions in the app.
 */
class MeshTopLevelNavigation(private val navController: NavHostController) {

    fun navigateTo(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            // Pop up to the start destination of the graph to
            // avoid building up a large stack of destinations
            // on the back stack as users select items
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
            // Restore state when reselecting a previously selected item
            restoreState = true
        }
    }
}

data class TopLevelDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconTextId: Int
)

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(
        route = NodesDestination.route,
        selectedIcon = Icons.Filled.Hive,
        unselectedIcon = Icons.Outlined.Hive,
        iconTextId = R.string.label_nav_bar_nodes
    ),
    TopLevelDestination(
        route = GroupsDestination.route,
        selectedIcon = Icons.Filled.GroupWork,
        unselectedIcon = Icons.Outlined.GroupWork,
        iconTextId = R.string.label_nav_bar_groups
    ),
    TopLevelDestination(
        route = ProxyFilterDestination.route,
        selectedIcon = Icons.Filled.Hub,
        unselectedIcon = Icons.Outlined.Hub,
        iconTextId = R.string.label_nav_bar_proxy_filter
    ),
    TopLevelDestination(
        route = SettingsDestination.route,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconTextId = R.string.label_nav_bar_settings
    )
)
