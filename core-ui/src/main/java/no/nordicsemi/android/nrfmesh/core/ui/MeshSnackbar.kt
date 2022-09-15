package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Shows a snackbar executes the lambdas given for [SnackbarResult.Dismissed] and
 * [SnackbarResult.ActionPerformed]. Invoke this with launched effect to handle configuration
 * changes.
 *
 * @param snackbarHostState     SnackbarHostState of the scaffold.
 * @param message               Message to be displayed.
 * @param actionLabel           Action label to be displayed.
 * @param duration              Duration as to how long the snackbar should be displayed.
 * @param onDismissed           Specifies an optional action to be invoked when the snackbar
 *                              is dismissed by the system or the user.
 * @param onActionPerformed     Specifies an optional action to be invoked when the snackbar action
 *                              is performed.
 */
suspend fun showSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onDismissed: () -> (Unit) = {},
    onActionPerformed: () -> (Unit) = {}
) {
    // Let's dismiss any snackbar that's been shown already.
    // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#Snackbar(androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function0)
    snackbarHostState.currentSnackbarData?.dismiss()
    when (snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)) {
        SnackbarResult.Dismissed -> onDismissed()
        SnackbarResult.ActionPerformed -> onActionPerformed()
    }
}

/**
 * Shows a snackbar with a given coroutine scope and executes the lambdas given for
 * [SnackbarResult.Dismissed] and [SnackbarResult.ActionPerformed].
 *
 * @param scope                 Coroutine scope,
 * @param snackbarHostState     SnackbarHostState of the scaffold.
 * @param message               Message to be displayed.
 * @param actionLabel           Action label to be displayed.
 * @param duration              Duration as to how long the snackbar should be displayed.
 * @param onDismissed           Specifies an optional action to be invoked when the snackbar
 *                              is dismissed by the system or the user.
 * @param onActionPerformed     Specifies an optional action to be invoked when the snackbar action
 *                              is performed.
 */
fun showSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onDismissed: () -> (Unit) = {},
    onActionPerformed: () -> (Unit) = {}
) {
    scope.launch {
        // Let's dismiss any snackbar that's been shown already.
        // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#Snackbar(androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function0)
        snackbarHostState.currentSnackbarData?.dismiss()
        when (snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)) {
            SnackbarResult.Dismissed -> onDismissed()
            SnackbarResult.ActionPerformed -> onActionPerformed()
        }
    }
}

/**
 * Shows a snackbar with a given coroutine scope and executes the lambdas given for
 * [SnackbarResult.Dismissed] and [SnackbarResult.ActionPerformed].
 *
 * @param scope                 Coroutine scope,
 * @param snackbarHostState     SnackbarHostState of the scaffold.
 * @param message               Message to be displayed.
 * @param duration              Duration as to how long the snackbar should be displayed.
 * @param onDismissed           Specifies an optional action to be invoked when the snackbar
 *                              is dismissed by the system or the user.
 */
fun showSnackbar(
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    message: String,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short,
    onDismissed: () -> (Unit) = {}
) {
    scope.launch {
        // Let's dismiss any snackbar that's been shown already.
        // https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#Snackbar(androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.ui.graphics.Shape,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,kotlin.Function0)
        // snackbarHostState.currentSnackbarData?.dismiss()
        when (snackbarHostState.showSnackbar(message, null, withDismissAction, duration)) {
            SnackbarResult.Dismissed -> onDismissed()
            else -> {}
        }
    }
}

