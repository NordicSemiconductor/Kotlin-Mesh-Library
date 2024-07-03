plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":mesh:logger"))
    implementation(libs.kotlinx.coroutines.core)
}