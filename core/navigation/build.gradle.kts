plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.library.compose)
    alias(libs.plugins.template.android.library.jacoco)
}

android {
    namespace = "android.template.core.navigation"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // Exposed so that feature modules and the app can implement/resolve NavKey destinations.
    api(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Local tests: jUnit
    testImplementation(libs.junit)
}
