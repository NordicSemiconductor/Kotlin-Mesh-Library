package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState

suspend fun showSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
) {
    snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
}