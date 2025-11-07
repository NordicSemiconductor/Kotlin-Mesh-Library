plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":mesh:logger"))
    implementation(nordic.kotlin.data)
    implementation(libs.kotlinx.coroutines.core)
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnitPlatform()
}