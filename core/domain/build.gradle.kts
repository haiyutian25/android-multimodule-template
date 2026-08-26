plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
    alias(libs.plugins.template.android.library.jacoco)
}

android {
    namespace = "android.template.core.domain"
}

dependencies {
    implementation(projects.core.data)

    implementation(libs.kotlinx.coroutines.android)
}
