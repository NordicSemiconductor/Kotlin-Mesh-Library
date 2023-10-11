plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.proxy"
}

dependencies {
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.scanner)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.permissions.ble)
    implementation(libs.kotlin.junit)

    implementation(project(":core-ui"))
    implementation(project(":core-data"))
    implementation(project(":mesh:core"))
    implementation(project(":mesh:bearer"))

    implementation(project(":feature-mesh-bearer-android"))
    implementation(project(":feature-mesh-bearer-gatt"))
    implementation("androidx.test:monitor:1.6.1")
    implementation("androidx.test.ext:junit-ktx:1.1.5")
}