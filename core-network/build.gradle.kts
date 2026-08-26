import com.android.build.api.variant.BuildConfigField
import java.io.StringReader
import java.util.Properties

plugins {
    alias(libs.plugins.template.android.library)
    alias(libs.plugins.template.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "android.template.core.network"
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.okhttp.logging)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlin.serialization)
}

// Reads BACKEND_URL from local.properties (gitignored, machine-specific), falling back to a
// placeholder. Set it like: BACKEND_URL=https://your.backend/  (must end with '/')
val backendUrl = providers.fileContents(
    rootProject.layout.projectDirectory.file("local.properties")
).asText.map { text ->
    val properties = Properties()
    properties.load(StringReader(text))
    properties.getProperty("BACKEND_URL") ?: "http://example.com/"
}.orElse("http://example.com/")

androidComponents {
    onVariants {
        it.buildConfigFields!!.put("BACKEND_URL", backendUrl.map { value ->
            BuildConfigField(type = "String", value = """"$value"""", comment = null)
        })
    }
}
