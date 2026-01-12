pluginManagement {
    repositories {
        google()              // ✅ DO NOT restrict
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()              // ✅ REQUIRED for Play Services
        mavenCentral()
    }
}

rootProject.name = "Echo"
include(":app")
