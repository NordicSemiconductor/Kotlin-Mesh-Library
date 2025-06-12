package no.nordicsemi.android.nrfmesh.feature.ivindex.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.feature.ivindex.IvIndexRoute
import no.nordicsemi.android.nrfmesh.feature.ivindex.IvIndexViewModel

@Parcelize
data object IvIndexContent : Parcelable

@Composable
fun IvIndexScreenRoute() {
    val viewModel = hiltViewModel<IvIndexViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    IvIndexRoute(
        isIvIndexChangeAllowed = uiState.isIvIndexChangeAllowed,
        ivIndex = uiState.ivIndex,
        onIvIndexChanged = viewModel::onIvIndexChanged,
        onIvIndexTestModeToggled = viewModel::toggleIvUpdateTestMode,
        testMode = uiState.testMode
    )
}