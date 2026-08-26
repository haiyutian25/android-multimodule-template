plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.sync.test"
}

dependencies {
    implementation(projects.coreData)
    implementation(projects.syncWork)

    implementation(libs.hilt.android.testing)
}
