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
 * @param snackbarHostState SnackbarHostState of the scaffold
 * @param message Message to be displayed.
 * @param actionLabel Action label to be displaed.
 * @param duration Duration as to how long the snackbar should be displayed for.
 * @param onDismissed Optional Action to perform when the snackbar is dismissed.
 * @param onActionPerformed Optional Action to perform when the snackbar action isperformed.
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
    when (snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)) {
        SnackbarResult.Dismissed -> onDismissed()
        SnackbarResult.ActionPerformed -> onActionPerformed()
    }
}

/**
 * Shows a snackbar with a given coroutine scope and executes the lambdas given for
 * [SnackbarResult.Dismissed] and [SnackbarResult.ActionPerformed].
 *
 * @param scope Coroutine scope
 * @param snackbarHostState SnackbarHostState of the scaffold
 * @param message Message to be displayed.
 * @param actionLabel Action label to be displaed.
 * @param duration Duration as to how long the snackbar should be displayed for.
 * @param onDismissed Optional Action to perform when the snackbar is dismissed.
 * @param onActionPerformed Optional Action to perform when the snackbar action isperformed.
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
        when (snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)) {
            SnackbarResult.Dismissed -> onDismissed()
            SnackbarResult.ActionPerformed -> onActionPerformed()
        }
    }
}

