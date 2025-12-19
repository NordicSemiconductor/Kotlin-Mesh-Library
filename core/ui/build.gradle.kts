plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidLibraryComposeConventionPlugin.kt
    alias(libs.plugins.nordic.library.compose)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.core.ui"
}

dependencies {
    api(nordic.ui)
    api(libs.androidx.compose.material.icons.extended)
    implementation(nordic.logger)
    implementation(nordic.log.timber)

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

    implementation("androidx.compose.material3:material3:1.4.0")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0")
    implementation("androidx.compose.material3.adaptive:adaptive:1.2.0")
    implementation("androidx.compose.material3.adaptive:adaptive-layout-android:1.2.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation-android:1.2.0")

    implementation(project(":mesh:core"))
}