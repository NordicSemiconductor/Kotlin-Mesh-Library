plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.kotlin.mesh.bearer.android"
}

dependencies {
    api(project(":mesh-bearer"))

    implementation("androidx.core:core-ktx:1.9.0")

    api("no.nordicsemi.android.kotlin.ble:core:0.0.1")
    api("no.nordicsemi.android.kotlin.ble:client:0.0.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}