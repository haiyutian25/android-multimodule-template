plugins {
    alias(libs.plugins.baselineprofile)
    alias(libs.plugins.template.android.test)
}

android {
    namespace = "android.template.benchmarks"

    defaultConfig {
        // Macrobenchmarks require API 23+, but Baseline Profile generation requires 28+.
        minSdk = 28
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":app"
    experimentalProperties["android.experimental.self-instrumenting"] = true

    // A Gradle Managed Device used to generate Baseline Profiles consistently.
    testOptions.managedDevices.localDevices {
        create("pixel6Api33") {
            device = "Pixel 6"
            apiLevel = 33
            systemImageSource = "aosp"
        }
    }
}

baselineProfile {
    // Use the managed device defined above for consistent Baseline Profile generation.
    managedDevices.clear()
    managedDevices += "pixel6Api33"
    useConnectedDevices = false
}

dependencies {
    implementation(libs.androidx.benchmark.macro)
    implementation(libs.androidx.test.core)
    implementation(libs.androidx.test.espresso.core)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.rules)
    implementation(libs.androidx.test.runner)
    implementation(libs.androidx.test.uiautomator)
}
