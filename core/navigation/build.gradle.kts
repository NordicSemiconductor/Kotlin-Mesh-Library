plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt
    alias(libs.plugins.nordic.library.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.core.navigation"
}

dependencies {
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:common"))
    // implementation(project(":core:ui"))

    implementation("androidx.compose.material:material-icons-extended-android:1.7.8")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0-alpha13")
}