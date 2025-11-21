plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.nordic.nexus.jvm)
}

group = "no.nordicsemi.kotlin.mesh"

nordicNexusPublishing {
    POM_ARTIFACT_ID = "crypto"
    POM_NAME = "Bluetooth Mesh Crypto Library"
    POM_DESCRIPTION = "Provides a set of cryptographic functions for the Kotlin Mesh Library."
    POM_URL = "https://github.com/NordicSemiconductor/Kotlin-Mesh-Library"
    POM_SCM_URL = "https://github.com/NordicSemiconductor/Kotlin-Mesh-Library"
    POM_SCM_CONNECTION = "scm:git@github.com:NordicSemiconductor/Kotlin-Mesh-Library.git"
    POM_SCM_DEV_CONNECTION = "scm:git@github.com:NordicSemiconductor/Kotlin-Mesh-Library.git"
}

dependencies {
    implementation(nordic.kotlin.data)
    implementation("org.bouncycastle:bcprov-jdk15to18:1.73")
    testImplementation(libs.kotlin.junit)
}