plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.proxy"
}

dependencies {

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.scanner)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.permissions.ble)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)

    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    implementation(project(":mesh:core"))
    implementation(project(":mesh:bearer"))

    implementation(project(":feature:mesh-bearer-android"))
    implementation(project(":feature:mesh-bearer-gatt"))
}