plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(nordic.kotlin.data) // -> nordic.kotlin.data
    implementation("org.bouncycastle:bcprov-jdk15to18:1.73")

    testImplementation(libs.kotlin.junit)
}