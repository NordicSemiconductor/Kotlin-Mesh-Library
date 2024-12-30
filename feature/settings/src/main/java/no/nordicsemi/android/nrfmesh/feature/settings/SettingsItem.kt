package no.nordicsemi.android.nrfmesh.feature.settings

import android.os.Parcelable
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.parcelize.Parcelize

internal data class SettingsItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val editable: Boolean = false,
    val placeHolderText: String = "",
    val onClick: (() -> Unit) = { }
)

@Parcelize
enum class SettingsItemRoute(val id: Int) : Parcelable {
    NAME(0),
    PROVISIONERS(1),
    NETWORK_KEYS(2),
    APPLICATION_KEYS(3),
    SCENES(4)
}