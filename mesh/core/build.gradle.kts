plugins {
    id("kotlin")
    alias(libs.plugins.kotlin.serialization)
    id("org.jetbrains.dokka")
}
dependencies {
    api(project(":mesh:crypto"))
    implementation(project(":mesh:bearer"))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    // Instant is used as a part of the API
    api("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")

    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}