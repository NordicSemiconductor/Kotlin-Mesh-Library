plugins {
    id("kotlin")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation(project(":mesh-core"))
    api(project(":mesh-bearer-provisioning"))
    implementation(project(":mesh-crypto"))
    testImplementation("junit:junit:4.13.2")
}