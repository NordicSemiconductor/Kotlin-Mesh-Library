pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        mavenCentral()
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
            from("no.nordicsemi.android.gradle:version-catalog:1.5.6")
        }
    }
}
rootProject.name = "Kotlin-nRF Mesh-Library"
include(":app")
include(":core-ui")
include(":core-data-storage")
include(":feature-nodes")
include(":feature-groups")
include(":feature-settings")
include(":feature-proxy-filter")
include(":core-data")
include(":feature-export")
include(":feature-network-keys")
include(":core-common")
include(":feature-application-keys")
include(":feature-scenes")
include(":feature-provisioners")
include(":feature-mesh-bearer-android")
include(":feature-mesh-bearer-gatt")
include(":feature-mesh-bearer-pbgatt")

include(":mesh:mesh-core")
include(":mesh:mesh-crypto")
include(":mesh:mesh-provisioning")
include(":mesh:mesh-configuration")
include(":mesh:mesh-generic")
include(":mesh:mesh-lighting")
include(":mesh:mesh-logger")
include(":mesh:mesh-bearer")
include(":mesh:mesh-bearer-provisioning")