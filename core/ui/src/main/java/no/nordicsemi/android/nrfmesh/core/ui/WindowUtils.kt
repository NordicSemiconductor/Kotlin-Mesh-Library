package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.window.core.layout.WindowSizeClass

@Composable
fun isCompactWidth(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    // use bounds checks
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(
            widthDpBreakpoint = WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
        ) -> false

        windowSizeClass.isWidthAtLeastBreakpoint(
            widthDpBreakpoint = WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
        ) -> true

        else -> true
    }
}