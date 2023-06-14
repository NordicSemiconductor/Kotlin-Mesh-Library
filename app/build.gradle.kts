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
    implementation(libs.nordic.uiscanner)
    implementation(libs.nordic.permission)
    // Workaround to get access to the scanner compat api
    implementation(libs.nordic.scanner)

    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.activity.compose)

    implementation(libs.accompanist.systemuicontroller)
    implementation(libs.accompanist.swiperefresh)

    implementation(libs.timber)

    implementation(project(":core-ui"))
    implementation(project(":core-common"))
    implementation(project(":core-data"))
    implementation(project(":core-data-storage"))
    implementation(project(":feature-nodes"))
    implementation(project(":feature-groups"))
    implementation(project(":feature-proxy-filter"))
    implementation(project(":feature-settings"))
    implementation(project(":feature-network-keys"))
    implementation(project(":feature-application-keys"))
    implementation(project(":feature-scenes"))
    implementation(project(":feature-provisioners"))
    implementation(project(":feature-mesh-bearer-android"))
    implementation(project(":feature-mesh-bearer-pbgatt"))
    implementation(project(":feature-export"))
    implementation(project(":mesh:mesh-core"))
    implementation(project(":mesh:mesh-provisioning"))

    implementation(libs.nordic.blek.core)
    implementation(libs.nordic.blek.client)
    implementation(libs.nordic.blek.scanner)

    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.11")
}
