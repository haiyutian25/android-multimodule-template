plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.room)
    alias(libs.plugins.template.android.hilt)
    alias(libs.plugins.template.android.library.jacoco)
}

android {
    namespace = "android.template.core.database"

    defaultConfig {
        testInstrumentationRunner = "android.template.core.testing.HiltTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(projects.core.model)
}
