package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun isCompactWidth(): Boolean {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    return when {
        windowSizeClass.isWidthAtLeastBreakpoint(widthDpBreakpoint = 840.dp.value.toInt()) -> false
        windowSizeClass.isWidthAtLeastBreakpoint(widthDpBreakpoint = 600.dp.value.toInt()) -> true
        else -> true
    }
}