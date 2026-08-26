plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.sync.test"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.sync.work)

    implementation(libs.hilt.android.testing)
}
