import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import template.buildlogic.libs

/**
 * Convention plugin for feature "impl" modules.
 *
 * Feature impl modules host the UI (Compose screens + ViewModels) for a feature. This plugin
 * centralizes the dependencies every feature impl module needs, so individual feature build files
 * only declare their feature-specific dependencies.
 */
class AndroidFeatureImplConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "template.android.library")
            apply(plugin = "template.android.hilt")

            dependencies {
                // Shared UI building blocks (theme, common composables).
                "implementation"(project(":core:ui"))

                // Lifecycle + Compose integration.
                "implementation"(libs.findLibrary("androidx-lifecycle-runtime-compose").get())
                "implementation"(libs.findLibrary("androidx-lifecycle-viewmodel-compose").get())
                "implementation"(libs.findLibrary("androidx-hilt-lifecycle-viewmodel-compose").get())

                // Navigation3 runtime for entry providers / NavKey handling.
                "implementation"(libs.findLibrary("androidx-navigation3-runtime").get())
            }
        }
    }
}
