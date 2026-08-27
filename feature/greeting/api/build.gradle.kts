plugins {
    alias(libs.plugins.template.android.feature.api)
}

android {
    namespace = "android.template.feature.greeting.api"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}
