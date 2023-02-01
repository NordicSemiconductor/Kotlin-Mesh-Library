plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidLibraryConventionPlugin.kt
    alias(libs.plugins.nordic.library)
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidHiltConventionPlugin.kt
    alias(libs.plugins.nordic.hilt)
}


android {
    namespace = "no.nordicsemi.android.nrfmesh.core.data"
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-data-storage"))
    implementation(project(":mesh-core"))
}