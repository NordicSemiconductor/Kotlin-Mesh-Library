@file:OptIn(ExperimentalComposeUiApi::class)

package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties

/**
 * Common Alert Dialog composable to maintain consistency.
 *
 * @param onDismissRequest              Called when the user tries to dismiss the Dialog by clicking
 *                                      outside or pressing the back button. This is not called when
 *                                      the dismiss button is clicked.
 * @param confirmButtonText             Button which is meant to confirm a proposed action, thus
 *                                      resolving what triggered the dialog. The dialog does not set
 *                                      up any events for this button so they need to be set up by
 *                                      the caller.
 * @param dismissButtonText             Button which is meant to dismiss the dialog. The dialog does
 *                                      not set up any events for this button so they need to be set
 *                                      up by the caller.
 * @param icon                          Optional icon that will appear above the [title] or above
 *                                      the [text], in case a title was not provided.
 * @param title                         Title which should specify the purpose of the dialog. The
 *                                      title is not mandatory, because there may be sufficient
 *                                      information inside the [text].
 * @param error                         When true the confirm button will be disabled
 * @param content                       Content of the dialog body.
 */
@Composable
fun MeshAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String = stringResource(id = R.string.confirm),
    onConfirmClick: () -> Unit,
    dismissButtonText: String = stringResource(id = R.string.cancel),
    onDismissClick: () -> Unit,
    icon: ImageVector? = null,
    title: String? = null,
    error: Boolean = false,
    content: @Composable () -> Unit = {}
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.85f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            Button(
                enabled = !error,
                onClick = { onConfirmClick() }
            ) { Text(text = confirmButtonText) }
        },
        dismissButton = {
            Button(onClick = { onDismissClick() }) { Text(text = dismissButtonText) }
        },
        icon = {
            icon?.let { Icon(imageVector = it, contentDescription = null, tint = Color.Red) }
        },
        title = { title?.let { Text(text = it) } },
        text = content
    )
}

/**
 * Common Alert Dialog composable to maintain consistency.
 *
 * @param onDismissRequest              Called when the user tries to dismiss the Dialog by clicking
 *                                      outside or pressing the back button. This is not called when
 *                                      the dismiss button is clicked.
 * @param confirmButtonText             Button which is meant to confirm a proposed action, thus
 *                                      resolving what triggered the dialog. The dialog does not set
 *                                      up any events for this button so they need to be set up by
 *                                      the caller.
 * @param dismissButtonText             Button which is meant to dismiss the dialog. The dialog does
 *                                      not set up any events for this button so they need to be set
 *                                      up by the caller.
 * @param icon                          Optional icon that will appear above the [title] or above
 *                                      the [text], in case a title was not provided.
 * @param title                         Title which should specify the purpose of the dialog. The
 *                                      title is not mandatory, because there may be sufficient
 *                                      information inside the [text].
 * @param text                          Text which presents the details regarding the dialog's
 *                                      purpose.
 */
@Composable
fun MeshAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButtonText: String = stringResource(id = R.string.confirm),
    onConfirmClick: () -> Unit,
    dismissButtonText: String = stringResource(id = R.string.cancel),
    onDismissClick: () -> Unit,
    icon: ImageVector? = null,
    title: String? = null,
    text: String? = null
) {
    AlertDialog(
        modifier = Modifier.fillMaxWidth(0.85f),
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            Button(onClick = { onConfirmClick() }) { Text(text = confirmButtonText) }
        },
        dismissButton = {
            Button(onClick = { onDismissClick() }) { Text(text = dismissButtonText) }
        },
        icon = {
            icon?.let { Icon(imageVector = it, contentDescription = null, tint = Color.Red) }
        },
        title = { title?.let { Text(text = it) } },
        text = { text?.let { Text(text = it) } }
    )
}