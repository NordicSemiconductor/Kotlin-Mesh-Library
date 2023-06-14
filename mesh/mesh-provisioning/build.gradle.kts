plugins {
    id("kotlin")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    api(project(":mesh:mesh-core"))
    implementation(project(":mesh:mesh-bearer-provisioning"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.1")
}