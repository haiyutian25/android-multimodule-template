import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt.gradle)
    alias(libs.plugins.ksp)
}

android {
    namespace = "android.template.sync.work"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        aidl = false
        buildConfig = false
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
    implementation(project(":core-data"))

    // WorkManager
    implementation(libs.androidx.work.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Hilt Dependency Injection (including HiltWorker support)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    // api: the public HiltWorkerFactoryEntryPoint exposes the HiltWorkerFactory type to consumers
    api(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
