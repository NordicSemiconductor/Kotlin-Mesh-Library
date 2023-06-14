plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.groups"
}

dependencies {
    implementation(libs.nordic.navigation)
    implementation(libs.androidx.lifecycle.runtime.compose)

    implementation(project(":core-ui"))
    implementation(project(":core-data"))
    implementation(project(":mesh:core"))

    implementation(libs.nordic.navigation)
}