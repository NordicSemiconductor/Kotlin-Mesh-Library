plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt
    alias(libs.plugins.nordic.library.compose)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.core.ui"
}

dependencies {
    api(libs.nordic.theme)
    api(libs.nordic.ui)
    api(libs.androidx.compose.material.iconsExtended)
    implementation(libs.nordic.logger)
    implementation(libs.nordic.log.timber)

    implementation(project(":core:common"))
    implementation(project(":core:navigation"))

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)

    implementation("androidx.compose.material3:material3:1.4.0-alpha05")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite-android:1.4.0-alpha05")
    implementation("androidx.compose.material3.adaptive:adaptive:1.1.0-alpha08")
    implementation("androidx.compose.material3.adaptive:adaptive-layout-android:1.1.0-alpha08")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation-android:1.1.0-alpha08")
}