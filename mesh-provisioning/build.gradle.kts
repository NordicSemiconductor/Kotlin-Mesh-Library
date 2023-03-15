plugins {
    id("kotlin")
}

dependencies {
    api(project(":mesh-core"))
    testImplementation("junit:junit:4.13.2")
}