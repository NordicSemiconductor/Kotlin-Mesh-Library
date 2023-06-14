plugins {
    id("kotlin")
    id("org.jetbrains.dokka")
}

dependencies {
    implementation("org.bouncycastle:bcprov-jdk15to18:1.73")
    testImplementation("junit:junit:4.13.2")
}