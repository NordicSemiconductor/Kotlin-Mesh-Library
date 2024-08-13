package no.nordicsemi.android.nrfmesh.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector

@Stable
abstract class AppState() {
    abstract val topLevelDestinations: List<TopLevelDestination>

    var currentScreen by mutableStateOf<Screen?>(null)
        protected set

    val showTopAppBar: Boolean
        get() = currentScreen?.showTopBar == true

    val navigationIcon: ImageVector
        get() = currentScreen?.navigationIcon ?: Icons.AutoMirrored.Outlined.ArrowBack

    val onNavigationIconClick: (() -> Unit)?
        get() = currentScreen?.onNavigationIconClick

    val title: String
        get() = currentScreen?.title.orEmpty()

    val actions: List<ActionItem>
        get() = currentScreen?.actions.orEmpty()

    val showBottomBar: Boolean
        get() = currentScreen?.showBottomBar ?: false

    protected abstract fun currentScreen()

    abstract fun navigate(destination: MeshNavigationDestination, route: String? = null)

    abstract fun onBackPressed()
}