plugins {
    alias(libs.plugins.template.android.feature.api)
}

android {
    namespace = "android.template.feature.mymodel.api"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}
