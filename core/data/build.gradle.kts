plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidFeatureConventionPlugin.kt
    alias(libs.plugins.nordic.feature)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.wire)
}


android {
    namespace = "no.nordicsemi.android.nrfmesh.core.data"
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

wire {
    kotlin {}
}

dependencies {
    implementation(nordic.permissions.ble)
    // Workaround to get access to the scanner compat api
    // implementation(nordic.scanner)
    // implementation(libs.kotlinx.datetime)

    // implementation(libs.androidx.dataStore.core)
    // implementation(libs.androidx.dataStore.preferences)

    implementation("androidx.datastore:datastore-core:1.2.0-rc01")
    implementation("androidx.datastore:datastore-preferences:1.2.0-rc01")
    implementation("androidx.datastore:datastore-preferences-proto:1.2.0-rc01")

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)

    implementation(project(":core:ui"))
    api(project(":core:common"))
    implementation(project(":mesh:core"))
    implementation(project(":mesh:bearer-pbgatt"))
    implementation(project(":mesh:bearer-gatt"))
    implementation(nordic.blek.client.android)
}