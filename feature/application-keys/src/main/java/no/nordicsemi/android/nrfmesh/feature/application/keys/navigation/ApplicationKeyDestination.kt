package no.nordicsemi.android.nrfmesh.feature.application.keys.navigation

import android.os.Parcelable
import androidx.compose.runtime.Composable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import no.nordicsemi.android.nrfmesh.feature.application.keys.ApplicationKeyRoute
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.KeyIndex
import no.nordicsemi.kotlin.mesh.core.model.NetworkKey

@Serializable
@Parcelize
data class ApplicationKeyContent(val keyIndex: KeyIndex) : Parcelable

@Composable
fun ApplicationKeyScreenRoute(
    key: ApplicationKey,
    networkKeys: List<NetworkKey>,
    save: () -> Unit,
) {
    ApplicationKeyRoute(key = key, networkKeys = networkKeys, save = save)
}