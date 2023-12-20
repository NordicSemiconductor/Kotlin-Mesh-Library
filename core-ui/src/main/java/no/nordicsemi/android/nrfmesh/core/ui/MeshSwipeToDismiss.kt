package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Holds the Swipe to dismiss composable, its animation and the current state
 */
@Composable
@ExperimentalMaterial3Api
fun SwipeDismissItem(
    dismissState: DismissState,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = !(dismissState.dismissDirection?.let { dismissState.isDismissed(it) } ?: false),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        SwipeToDismissBox(
            state = dismissState,
            backgroundContent = {
                val color by animateColorAsState(targetValue = Color.Red, label = "dismiss")
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (dismissState.dismissDirection == DismissDirection.StartToEnd)
                        Alignment.CenterStart
                    else Alignment.CenterEnd
                ) {
                    Icon(imageVector = Icons.Outlined.Delete, contentDescription = "null")
                }
            }
        ) {
            content()
            HorizontalDivider()
        }
    }
}