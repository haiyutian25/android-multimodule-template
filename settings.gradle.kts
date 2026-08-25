pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Multimodule template"

include(":app")
include(":core-data")
include(":core-database")
include(":core-domain")
include(":core-model")
include(":core-testing")
include(":core-ui")
include(":feature-mymodel-api")
include(":feature-mymodel-impl")
include(":sync-work")
include(":test-app")
