plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.elements"
}

dependencies {

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.nordic.kotlin.data)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)



    implementation("androidx.compose.material3:material3:1.4.0-alpha07")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0-alpha07")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha07")

    implementation("androidx.compose.material3.adaptive:adaptive:1.1.0-beta01")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.1.0-beta01")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0-beta01")

    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:ui"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:mesh-bearer-android"))
    implementation(project(":mesh:core"))
}