package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun isCompactWidth(): Boolean = with(currentWindowAdaptiveInfo()) {
    windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
}

