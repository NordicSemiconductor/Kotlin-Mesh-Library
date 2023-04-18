plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.nodes"
}

dependencies {
    implementation(libs.nordic.navigation)


    implementation(project(":core-ui"))
    implementation(project(":core-data"))

    implementation(project(":mesh-core"))
    implementation (project(":feature-scanner"))
}