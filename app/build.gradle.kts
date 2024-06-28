plugins {
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidApplicationComposeConventionPlugin.kt
    alias(libs.plugins.nordic.application.compose)
    // https://github.com/NordicSemiconductor/Android-Gradle-Plugins/blob/main/plugins/src/main/kotlin/AndroidHiltConventionPlugin.kt
    alias(libs.plugins.nordic.hilt)
}

android {
    namespace = "no.nordicsemi.android.nrfmesh"
    defaultConfig {
        applicationId = "no.nordicsemi.android.nrfmesh"
    }
}

dependencies {

    implementation(libs.nordic.theme)
    implementation(libs.nordic.navigation)
    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.scanner)
    implementation(libs.nordic.blek.uiscanner)
    implementation(libs.nordic.permissions.ble)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.accompanist.systemuicontroller)

    implementation(libs.timber)

    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":core-data"))
    implementation(project(":feature-nodes"))
    implementation(project(":feature-groups"))
    implementation(project(":feature-proxy"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-network-keys"))
    implementation(project(":feature-application-keys"))
    implementation(project(":feature-scenes"))
    implementation(project(":feature-provisioners"))
    implementation(project(":feature-mesh-bearer-android"))
    implementation(project(":feature-mesh-bearer-pbgatt"))
    implementation(project(":feature-export"))
    implementation(project(":mesh:core"))
    implementation(project(":mesh:provisioning"))

     debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")

    testImplementation(libs.junit4)
    testImplementation(libs.kotlin.junit)
    testImplementation(libs.androidx.test.ext)
    testImplementation(libs.androidx.test.rules)

    androidTestImplementation(libs.junit4)
    androidTestImplementation(libs.kotlin.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.rules)
}
