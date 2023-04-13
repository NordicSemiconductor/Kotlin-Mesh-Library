plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.kotlin.mesh.bearer.pbgatt"
}

dependencies {
    api(project(":mesh-bearer-android"))
    api(project(":mesh-bearer-provisioning"))
    implementation("androidx.test.ext:junit-ktx:1.1.5")

    api("no.nordicsemi.android.kotlin.ble:core:0.0.1")
    api("no.nordicsemi.android.kotlin.ble:client:0.0.1")

    testImplementation("junit:junit:4.13.2")
}