plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    api(project(":mesh:provisioning"))
    api(project(":mesh:bearer-gatt"))
    testImplementation("junit:junit:4.13.2")
}