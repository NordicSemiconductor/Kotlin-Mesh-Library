package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Common outlined button composable that is used throughout the app.
 *
 * @param modifier                  Modifier to be used for the button.
 * @param isOnClickActionInProgress Boolean flag that will indicate if the action is in progress and
 *                                  this will show a progress indicator in front of the button text.
 * @param buttonIcon                ImageVector to be shown int he button icon.
 * @param text                      Text to be shown on the button.
 * @param onClick                   Action to be performed on button click.
 * @param enabled                   Flag to enable or disable the button.
 *
 */
@Composable
fun MeshOutlinedButton(
    modifier: Modifier = Modifier,
    isOnClickActionInProgress: Boolean = false,
    buttonIcon: ImageVector? = null,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    OutlinedButton(
        modifier = modifier.defaultMinSize(minWidth = 120.dp),
        enabled = enabled,
        onClick = onClick,
        content = {
            if (isOnClickActionInProgress) {
                CircularProgressIndicator(modifier = Modifier.size(size = 24.dp))
            } else {
                buttonIcon?.let { Icon(imageVector = it, contentDescription = null) }
            }
            Text(modifier = modifier.padding(start = 8.dp), text = text)
        }
    )
}