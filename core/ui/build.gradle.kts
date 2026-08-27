plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.library.compose)
    alias(libs.plugins.template.android.library.jacoco)
}

android {
    namespace = "android.template.core.ui"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(projects.core.designsystem)
}
