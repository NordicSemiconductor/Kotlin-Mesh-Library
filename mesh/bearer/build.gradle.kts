plugins {
    id("kotlin")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    api(project(":mesh:logger"))
}