plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":mesh:logger"))
    implementation(libs.nordic.kotlin.data)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation("junit:junit:4.13.2")
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}