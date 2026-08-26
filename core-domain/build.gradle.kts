plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.core.domain"
}

dependencies {
    implementation(projects.coreData)

    implementation(libs.kotlinx.coroutines.android)
}
