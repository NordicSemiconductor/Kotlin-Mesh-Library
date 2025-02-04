plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.settings"
}

dependencies {

    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)

    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:navigation"))
    implementation(project(":feature:export"))
    implementation(project(":feature:provisioners"))
    implementation(project(":feature:network-keys"))
    implementation(project(":feature:application-keys"))
    implementation(project(":feature:scenes"))
    implementation(project(":mesh:core"))

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)

    implementation("androidx.compose.material3:material3:1.4.0-alpha06")
    implementation("androidx.compose.material3:material3-window-size-class:1.4.0-alpha06")
    implementation("androidx.compose.material3:material3-adaptive-navigation-suite:1.4.0-alpha06")

    implementation("androidx.compose.material3.adaptive:adaptive:1.1.0-beta01")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.1.0-beta01")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.1.0-beta01")

}