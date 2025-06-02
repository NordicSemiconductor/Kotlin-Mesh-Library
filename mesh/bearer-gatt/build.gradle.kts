plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":mesh:bearer"))
    api("no.nordicsemi.kotlin.ble:client-core:2.0.0-alpha02")
    testImplementation("junit:junit:4.13.2")
}