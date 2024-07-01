plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.wire)
}


android {
    namespace = "no.nordicsemi.android.nrfmesh.core.data"
}

wire {
    kotlin {}
}

dependencies {
    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.scanner)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.permissions.ble)
    // Workaround to get access to the scanner compat api
    implementation(libs.nordic.scanner)

    implementation(libs.androidx.dataStore.core)
    implementation(libs.androidx.dataStore.preferences)

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
    implementation(project(":mesh:core"))
    implementation(project(":mesh:provisioning"))
    implementation(project(":feature:mesh-bearer-android"))
    implementation(project(":feature:mesh-bearer-pbgatt"))
    implementation(project(":feature:mesh-bearer-gatt"))
}