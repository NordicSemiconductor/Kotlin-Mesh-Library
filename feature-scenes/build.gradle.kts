plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.feature.scenes"
}

dependencies {
    implementation(project(":core-data"))
    implementation(project(":core-ui"))

    implementation(project(":mesh:mesh-core"))

    implementation(libs.nordic.navigation)
}