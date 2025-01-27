package no.nordicsemi.android.nrfmesh.feature.settings

/**
 * ClickableSetting is a sealed class that represents the clickable settings item in the settings
 * screen. This is used to highlight the selected item in the settings list ui
 */
sealed class ClickableSetting {
    data object Provisioners : ClickableSetting()
    data object NetworkKeys : ClickableSetting()
    data object ApplicationKeys : ClickableSetting()
    data object Scenes : ClickableSetting()
}