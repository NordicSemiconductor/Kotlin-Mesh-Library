package no.nordicsemi.android.nrfmesh.feature.network.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import no.nordicsemi.android.nrfmesh.feature.network.keys.NetworkKeyRoute
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Parcelize
data class NetworkKeyRoute(val keyIndex: KeyIndex) : Parcelable

@Composable
fun NetworkKeyScreenRoute(
    key: NetworkKey,
    save: () -> Unit,
) {
    NetworkKeyRoute(key = key, save = save)
}