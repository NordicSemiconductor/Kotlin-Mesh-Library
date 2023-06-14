plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.kotlin.mesh.bearer.pbgatt"
}

dependencies {
    api(project(":feature-mesh-bearer-android"))
    api(project(":mesh:mesh-bearer-provisioning"))
    implementation(project(":mesh:mesh-provisioning"))
    implementation("androidx.test.ext:junit-ktx:1.1.5")

    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)

    testImplementation("junit:junit:4.13.2")
}