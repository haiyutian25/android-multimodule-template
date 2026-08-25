import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION") // Remove when fixed https://youtrack.jetbrains.com/issue/KTIJ-19369
plugins {
    alias(libs.plugins.android.test)

    alias(libs.plugins.ksp)
}

android {
    namespace = "android.template.test.navigation"
    compileSdk = 36
    targetProjectPath = ":app"

    defaultConfig {
        minSdk = 23
        targetSdk = 36

        testInstrumentationRunner = "android.template.core.testing.HiltTestRunner"
    }

    buildFeatures {
        aidl = false
        buildConfig = false
        renderScript = false
        shaders = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":app"))
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    implementation(project(":core-data"))
    implementation(project(":core-domain"))
    implementation(project(":core-testing"))
    implementation(project(":feature-mymodel-api"))
    implementation(project(":feature-mymodel-impl"))
    implementation(project(":sync-work"))

    // Testing
    implementation(libs.androidx.test.core)

    // Hilt and instrumented tests.
    implementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)

    // Compose
    implementation(libs.androidx.compose.ui.test.junit4)
}
