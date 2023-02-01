plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidLibraryConventionPlugin.kt
    alias(libs.plugins.nordic.library.compose)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.core.navigation"
}

dependencies {
    implementation(libs.nordic.navigation)
}