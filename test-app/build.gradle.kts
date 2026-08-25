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
    implementation(project(":app"))
    implementation(project(":core-data"))
    implementation(project(":core-domain"))
    implementation(project(":core-testing"))
    implementation(project(":feature-mymodel-api"))
    implementation(project(":feature-mymodel-impl"))
    implementation(project(":sync-work"))

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
