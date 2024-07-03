plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.settings"
}

dependencies {

    implementation(libs.nordic.navigation)
    implementation(libs.kotlinx.datetime)

    implementation(project(":core-ui"))
    implementation(project(":core-data"))
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

}