plugins {
    alias(libs.plugins.template.jvm.library)
    alias(libs.plugins.template.android.hilt)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
