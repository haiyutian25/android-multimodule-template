plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.room)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.core.database"

    defaultConfig {
        testInstrumentationRunner = "android.template.core.testing.HiltTestRunner"
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(project(":core-model"))
}
