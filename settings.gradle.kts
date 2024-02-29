pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    plugins {
        id("org.jetbrains.dokka") version "1.9.20"
        id("org.jetbrains.kotlin.jvm") version "2.0.0"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://androidx.dev/storage/compose-compiler/repository/")
    }
    versionCatalogs {
        create("libs") {
            from("no.nordicsemi.android.gradle:version-catalog:2.3")
        }
    }
}
rootProject.name = "Kotlin-nRF Mesh-Library"
include(":app")
include(":core-ui")
include(":core-common")
include(":core-data")
include(":feature:nodes")
include(":feature:groups")
include(":feature:settings")
include(":feature:proxy")
include(":feature:export")
include(":feature:network-keys")
include(":feature:application-keys")
include(":feature:scenes")
include(":feature:provisioners")
include(":feature:mesh-bearer-android")
include(":feature:mesh-bearer-gatt")
include(":feature:mesh-bearer-pbgatt")

include(":mesh:core")
include(":mesh:crypto")
include(":mesh:provisioning")
include(":mesh:configuration")
include(":mesh:generic")
include(":mesh:lighting")
include(":mesh:logger")
include(":mesh:bearer")
include(":mesh:bearer-provisioning")
include(":mesh:foundation")

/*if (file("../Android-Gradle-Plugins").exists()) {
    includeBuild("../Android-Gradle-Plugins")
}*/
if (file("../Kotlin-Util-Library").exists()) {
    includeBuild("../Kotlin-Util-Library")
}
include(":feature:config-network-keys")
