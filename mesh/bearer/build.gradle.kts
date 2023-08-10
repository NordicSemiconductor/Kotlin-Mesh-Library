plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.2")
    api(project(":mesh:logger"))
}