plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
}

android {
    namespace = "android.template.sync.work"
}

dependencies {
    implementation(projects.core.data)

    // WorkManager
    implementation(libs.androidx.work.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // HiltWorker support
    // api: the public HiltWorkerFactoryEntryPoint exposes the HiltWorkerFactory type to consumers
    api(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
}
