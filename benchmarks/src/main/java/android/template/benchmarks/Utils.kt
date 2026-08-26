package android.template.benchmarks

import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until

/**
 * The package name of the app under test. The customizer rewrites this along with the
 * application package.
 */
const val PACKAGE_NAME = "android.template"

/**
 * Waits until an object with [selector] is visible on screen and returns the object.
 * If the element is not available in [timeout], throws [AssertionError].
 */
fun UiDevice.waitAndFindObject(selector: BySelector, timeout: Long): UiObject2 {
    if (!wait(Until.hasObject(selector), timeout)) {
        throw AssertionError("Element not found on screen in ${timeout}ms (selector=$selector)")
    }

    return findObject(selector)
}
