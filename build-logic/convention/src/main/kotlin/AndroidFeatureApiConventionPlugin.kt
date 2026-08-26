import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import template.buildlogic.libs

/**
 * Convention plugin for feature "api" modules.
 *
 * Feature api modules expose the navigation contract (NavKey destinations) that other modules and
 * the app navigate to. This plugin centralizes the dependencies every feature api module needs.
 */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "template.android.library")
            apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

            dependencies {
                // Exposes core:navigation (and transitively navigation3-runtime) so feature api
                // modules can declare NavKey destinations.
                "api"(project(":core:navigation"))
                // NavKey implementations are @Serializable.
                "implementation"(libs.findLibrary("kotlinx-serialization-core").get())
            }
        }
    }
}
