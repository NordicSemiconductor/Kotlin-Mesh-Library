plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("no.nordicsemi.kotlin:data:0.1.0") // -> libs.nordic.kotlin.data
    api(project(":mesh:core"))
    implementation(project(":mesh:bearer-provisioning"))

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}