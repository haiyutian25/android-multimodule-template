plugins {
    alias(libs.plugins.template.android.test)
    alias(libs.plugins.ksp)
}

android {
    namespace = "android.template.test.navigation"
    targetProjectPath = ":app"

    defaultConfig {
        testInstrumentationRunner = "android.template.core.testing.HiltTestRunner"
    }
}

dependencies {
    implementation(projects.app)
    implementation(projects.core.data)
    implementation(projects.core.domain)
    implementation(projects.core.testing)
    implementation(projects.feature.mymodel.api)
    implementation(projects.feature.mymodel.impl)
    implementation(projects.sync.syncTest)
    implementation(projects.sync.work)

    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)

    // Testing
    implementation(libs.androidx.test.core)

    // Hilt and instrumented tests.
    implementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)

    // Compose
    implementation(libs.androidx.compose.ui.test.junit4)
}
