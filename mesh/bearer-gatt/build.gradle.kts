plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":mesh:bearer"))
    api(libs.nordic.blek.client.core)
    testImplementation("junit:junit:4.13.2")
}