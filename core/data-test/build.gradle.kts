plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.core.data.test"
}

dependencies {
    api(projects.core.data)

    implementation(libs.hilt.android.testing)
}
