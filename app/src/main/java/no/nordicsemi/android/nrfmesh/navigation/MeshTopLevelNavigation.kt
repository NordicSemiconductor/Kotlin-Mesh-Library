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

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Hive
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.GroupWork
import androidx.compose.material.icons.outlined.Hive
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import no.nordicsemi.android.nrfmesh.R
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupsRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesBaseRoute
import no.nordicsemi.android.nrfmesh.feature.nodes.navigation.NodesRoute
import no.nordicsemi.android.nrfmesh.feature.proxy.navigation.ProxyRoute
import no.nordicsemi.android.nrfmesh.feature.settings.navigation.SettingsRoute
import kotlin.reflect.KClass

/**
 * Routes for the different top level destinations in the application. Each of these destinations
 * can contain one or more screens (based on the window size). Navigation from one screen to the
 * next within a single destination will be handled directly in composable.
 */
enum class MeshTopLevelDestination(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    @StringRes val iconTextId: Int,
    @StringRes val titleTextId: Int,
    val route: KClass<*>,
    val baseRoute: KClass<*> = route,
) {
    NODES(
        selectedIcon = Icons.Filled.Hive,
        unselectedIcon = Icons.Outlined.Hive,
        iconTextId = R.string.label_nav_bar_nodes,
        titleTextId = R.string.label_nav_bar_nodes,
        route = NodesRoute::class,
        baseRoute = NodesBaseRoute::class
    ),
    GROUPS(
        selectedIcon = Icons.Filled.GroupWork,
        unselectedIcon = Icons.Outlined.GroupWork,
        iconTextId = R.string.label_nav_bar_groups,
        titleTextId = R.string.label_nav_bar_groups,
        route = GroupsRoute::class
    ),
    PROXY(
        selectedIcon = Icons.Filled.Hub,
        unselectedIcon = Icons.Outlined.Hub,
        iconTextId = R.string.label_nav_bar_proxy,
        titleTextId = R.string.label_nav_bar_proxy,
        route = ProxyRoute::class
    ),
    SETTINGS(
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        iconTextId = R.string.label_nav_bar_settings,
        titleTextId = R.string.label_nav_bar_settings,
        route = SettingsRoute::class
    )
}