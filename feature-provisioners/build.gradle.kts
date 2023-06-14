plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.feature.provisioners"
}

dependencies {
    implementation(project(":core-ui"))
    implementation(project(":core-data"))

    implementation(project(":mesh:mesh-core"))

    implementation(libs.nordic.navigation)
}