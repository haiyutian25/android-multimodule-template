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
rootProject.name = "multimodule-template"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include(":app")
include(":core-common")
include(":core-data")
include(":core-database")
include(":core-domain")
include(":core-model")
include(":core-network")
include(":core-testing")
include(":core-ui")
include(":feature-mymodel-api")
include(":feature-mymodel-impl")
include(":sync-work")
include(":test-app")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
    """
    This template requires JDK 17+ but it is currently using JDK ${JavaVersion.current()}.
    Java Home: [${System.getProperty("java.home")}]
    https://developer.android.com/build/jdks#jdk-config-in-studio
    """.trimIndent()
}
