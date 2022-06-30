package no.nordicsemi.android.nrfmesh.feature.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.nordicsemi.android.nrfmesh.core.ui.RowItem

@Composable
fun ExportRoute(viewModel: ExportViewModel = hiltViewModel()) {
    ExportScreen(viewModel = viewModel)
}

@Composable
private fun ExportScreen(viewModel: ExportViewModel) {
    LazyColumn {
        item {
            Text(
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                text = stringResource(R.string.label_configuration),
                style = MaterialTheme.typography.labelLarge
            )
            ExportEverything(viewModel = viewModel)
        }
        item {

        }
    }
}

@Composable
private fun ExportEverything(viewModel: ExportViewModel) {
    Row(
        modifier = Modifier
            .height(IntrinsicSize.Min)
            .fillMaxWidth()
            .padding(end = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RowItem(
            modifier = Modifier.weight(1f),
            imageVector = Icons.Outlined.FileDownload,
            title = stringResource(R.string.label_export_everything),
            subtitle = when (viewModel.exportUiState.isExportEverythingChecked) {
                true -> stringResource(R.string.label_export_everything_rationale)
                else -> ""
            }
        )
        Divider(
            modifier = Modifier
                .fillMaxHeight()
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .width(1.dp)
        )
        Switch(
            checked = viewModel.exportUiState.isExportEverythingChecked,
            onCheckedChange = {
                viewModel.onExportEverythingChecked(it)
            })
    }
}
