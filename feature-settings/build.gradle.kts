plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.settings"
}

dependencies {
    implementation(project(":core-data"))
    implementation(project(":core-ui"))
    implementation(project(":feature-export"))
    implementation(project(":feature-provisioners"))
    implementation(project(":feature-network-keys"))
    implementation(project(":feature-application-keys"))
    implementation(project(":feature-scenes"))

    implementation(project(":mesh:mesh-core"))

    implementation(libs.nordic.navigation)
}