package no.nordicsemi.android.nrfmesh.feature.scanner

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.ui.view.WarningView

@Composable
fun ScanEmptyView(locationRequiredAndDisabled: Boolean, navigateToLocationSettings: () -> Unit) {
    WarningView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        imageVector = Icons.AutoMirrored.Filled.BluetoothSearching,
        title = "CAN\'T SEE YOUR DEVICE?",
        hint = "1. Make sure the device is turned on and is connected to a power source." +
                "\n\n2. Make sure the appropriate firmware and SoftDevice are flashed.\n" +
                "   " + if (locationRequiredAndDisabled) {
            "\n\n" + "3. Location is turned off. Most Android phones " +
                    " require it in order to scan for Bluetooth LE devices. If you are sure your " +
                    " device is advertising and it doesn\'t show up here, click the button below to " +
                    " enable Location."
        } else {
            ""
        }.parseBold(),
        hintTextAlign = TextAlign.Justify,
    ) {
        if (locationRequiredAndDisabled) {
            Button(onClick = navigateToLocationSettings) {
                Text(text = "Enable location")
            }
        }
    }
}


fun String.parseBold(): AnnotatedString {
    val parts = this.split("<b>", "</b>")
    return buildAnnotatedString {
        var bold = false
        for (part in parts) {
            if (bold) {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(part)
                }
            } else {
                append(part)
            }
            bold = !bold
        }
    }
}