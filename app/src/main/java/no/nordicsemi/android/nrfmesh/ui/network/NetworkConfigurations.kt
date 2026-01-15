package no.nordicsemi.android.nrfmesh.ui.network

enum class NetworkConfigurations {
    EMPTY,
    CUSTOM,
    DEBUG,
    IMPORT;

    fun description(): String {
        return when (this) {
            EMPTY -> "Empty"
            CUSTOM -> "Custom"
            DEBUG -> "Debug"
            IMPORT -> "Import"
        }
    }
}