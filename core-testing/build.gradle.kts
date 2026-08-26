plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.core.testing"

    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    api(projects.coreCommon)
    api(projects.coreData)
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)

    implementation(libs.androidx.test.runner)
    implementation(libs.hilt.android.testing)
}
