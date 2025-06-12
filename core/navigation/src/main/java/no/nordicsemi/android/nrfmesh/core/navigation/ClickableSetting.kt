package no.nordicsemi.android.nrfmesh.core.navigation

import kotlinx.serialization.Serializable

/**
 * ClickableSetting is a sealed class that represents the clickable settings item in the settings
 * screen. This is used to highlight the selected item in the settings list ui
 */
@Serializable
enum class ClickableSetting {
    PROVISIONERS,
    NETWORK_KEYS,
    APPLICATION_KEYS,
    SCENES,
    IV_INDEX,
}