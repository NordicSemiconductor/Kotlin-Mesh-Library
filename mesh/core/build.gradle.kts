plugins {
    id("kotlin")
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.dokka")
}

dependencies {
    api(project(":mesh:crypto"))
    api(project(":mesh:logger"))
    implementation(project(":mesh:bearer"))
    implementation("no.nordicsemi.kotlin:data:0.1.0") // -> libs.nordic.kotlin.data

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    // Instant is used as a part of the API
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
}

tasks.test {
    useJUnitPlatform()
}