package android.template.benchmarks.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import android.template.benchmarks.PACKAGE_NAME
import org.junit.Rule
import org.junit.Test

/**
 * Baseline Profile for app startup. This profile also enables using Dex Layout Optimizations
 * via the `includeInStartupProfile` parameter.
 */
class StartupBaselineProfile {
    @get:Rule val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        PACKAGE_NAME,
        includeInStartupProfile = true,
        profileBlock = {
            startActivityAndWait()
        },
    )
}
