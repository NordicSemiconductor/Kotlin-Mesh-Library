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
    }
    versionCatalogs {
        create("libs") {
            from("no.nordicsemi.android.gradle:version-catalog:1.4.6")
        }
    }
}
rootProject.name = "Kotlin-nRF-Mesh-Library"
// include (":app")
include (":core-ui")
// include (":core-data-storage")
// include (":feature-nodes")
// include (":feature-groups")
// include (":feature-settings")
// include (":feature-proxy-filter")
// include (":core-data")
// include (":feature-export")
// include (":feature-network-keys")
// include (":core-common")
// include (":feature-application-keys")
// include (":feature-scenes")
// include (":feature-provisioners")
include (":mesh-core")
include (":mesh-crypto")
// include (":mesh-provisioning")
// include (":mesh-configuration")
// include (":mesh-generic")
// include (":mesh-lighting")
