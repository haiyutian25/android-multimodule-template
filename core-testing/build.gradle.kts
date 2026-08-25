plugins {
    alias(libs.plugins.template.android.library)
}

android {
    namespace = "android.template.core.testing"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    implementation(libs.androidx.test.runner)
    implementation(libs.hilt.android.testing)
}
