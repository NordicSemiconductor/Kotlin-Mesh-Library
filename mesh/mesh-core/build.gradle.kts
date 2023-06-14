plugins {
    id("kotlin")
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
}
dependencies {
    api(project(":mesh:mesh-crypto"))
    implementation(project(":mesh:mesh-bearer"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    // Instant is used as a part of the API
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
}

tasks.test {
    useJUnitPlatform()
}