package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import no.nordicsemi.android.common.theme.nordicDarkGray

@Composable
fun MeshNodeItem(
    nodeName: String,
    addressHex: String,
    companyName: String,
    elements: Int,
    models: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(all = 16.dp)
                .clickable(onClick = { onClick() })
        ) {
            Image(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .background(
                        color = MaterialTheme.colorScheme.nordicDarkGray,
                        shape = CircleShape
                    )
                    .padding(5.dp),
                painter = painterResource(R.drawable.ic_mesh_white),
                contentDescription = stringResource(R.string.description_mesh_icon)
            )
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(
                    text = nodeName,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.size(8.dp))
                Row {
                    Column {
                        Text(text = stringResource(R.string.label_address))
                        Text(text = stringResource(R.string.label_company))
                        Text(text = stringResource(R.string.label_elements))
                        Text(text = stringResource(R.string.label_models))
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Column {
                        Text(text = addressHex)
                        Text(text = companyName)
                        Text(text = "$elements")
                        Text(text = "$models")
                    }
                }
            }
        }
    }
}