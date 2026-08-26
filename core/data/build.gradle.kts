plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
    alias(libs.plugins.template.android.library.jacoco)
}

android {
    namespace = "android.template.core.data"

    defaultConfig {
        testInstrumentationRunner = "android.template.core.testing.HiltTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(projects.core.common)
    implementation(projects.core.database)
    implementation(projects.core.model)
    implementation(projects.core.network)

    implementation(libs.kotlinx.coroutines.android)

    // Local tests: jUnit, coroutines, Android runner
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
