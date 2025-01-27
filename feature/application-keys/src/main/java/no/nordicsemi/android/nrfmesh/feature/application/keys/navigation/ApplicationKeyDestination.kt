package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.core.navigation.AppState
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Serializable
@Parcelize
data class ApplicationKeyRoute(val keyIndex: KeyIndex) : Parcelable

internal fun NavGraphBuilder.applicationKeyGraph(
    appState: AppState,
    onBackPressed: () -> Unit,
) {
    composable(route = "") {

    }
}

@Composable
fun ApplicationKeyScreenRoute(key: ApplicationKey, networkKeys: List<NetworkKey>, save: () -> Unit) {
    ApplicationKeyRoute(key = key, networkKeys = networkKeys, save = save)
}