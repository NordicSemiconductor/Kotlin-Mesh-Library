plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(nordic.kotlin.data)
    implementation(libs.kotlinx.coroutines.core)
    api(project(":mesh:core"))
    api(project(":mesh:bearer-provisioning"))

    testImplementation("junit:junit:4.13.2")
    implementation(libs.kotlinx.coroutines.test)
}