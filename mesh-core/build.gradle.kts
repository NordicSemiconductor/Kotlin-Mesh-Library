plugins {
    id("kotlin")
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
}

dependencies {
    api(project(":mesh-crypto"))
    implementation(project(":mesh-bearer"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // Instant is used as a part of the API
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.platform:junit-platform-commons:1.5.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")
}
