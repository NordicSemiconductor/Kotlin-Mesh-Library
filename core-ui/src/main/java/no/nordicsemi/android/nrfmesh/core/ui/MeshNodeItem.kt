package no.nordicsemi.android.nrfmesh.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MeshNodeItem(
    nodeName: String,
    address: UShort,
    companyName: String,
    elements: Int,
    models: Int,
    onClick: () -> Unit
) {
    Row(modifier = Modifier.clickable(onClick = {
        onClick()
    })) {
        Image(
            painter = painterResource(R.drawable.ic_mesh_white),
            contentDescription = stringResource(R.string.description_mesh_icon)
        )
        Column {
            Text(text = nodeName)
            Text(text = "Address: $address")
            Text(text = "Company: $companyName")
            Text(text = "Elements: $elements")
            Text(text = "Models: $models")
        }
    }
}