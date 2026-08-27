package android.template.feature.greeting.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * UI tests for [GreetingScreen].
 */
@RunWith(AndroidJUnit4::class)
class GreetingScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun helloWorld_isDisplayed() {
        composeTestRule.setContent {
            GreetingScreen(onItemClick = {})
        }
        composeTestRule.onNodeWithText("Hello World").assertExists()
    }
}
