plugins {
    id("kotlin")
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.8.0"
}

dependencies {
    api(project(":mesh-crypto"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    testImplementation("junit:junit:4.13.2")
}
