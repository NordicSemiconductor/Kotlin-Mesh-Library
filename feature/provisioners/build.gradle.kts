plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh.feature.provisioners"
}

dependencies {
    implementation(libs.nordic.navigation)
    implementation("no.nordicsemi.kotlin:data:0.1.0") // -> libs.nordic.kotlin.data

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)

    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":core-data"))
    implementation(project(":mesh:core"))

}