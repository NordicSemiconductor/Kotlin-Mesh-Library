plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("no.nordicsemi.kotlin:data:0.1.0") // -> libs.nordic.kotlin.data
    implementation("org.bouncycastle:bcprov-jdk15to18:1.73")

    testImplementation(libs.kotlin.junit)
}