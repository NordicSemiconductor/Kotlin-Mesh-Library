plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.kotlin.mesh.bearer.gatt"
}

dependencies {
    api(project(":feature:mesh-bearer-android"))
    api(project(":mesh:bearer-provisioning"))

    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)
}
