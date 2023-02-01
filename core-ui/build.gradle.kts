plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt
    alias(libs.plugins.nordic.library.compose)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.core.ui"
}

dependencies {
    implementation(project(":mesh-core"))

    implementation(libs.nordic.theme)
    implementation(libs.nordic.uilogger)
    implementation(libs.nordic.uiscanner)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.permission)
    implementation(libs.nordic.log.timber)
    api("androidx.compose.material:material:1.3.1")
    api(libs.androidx.compose.material.iconsExtended)
}