package android.template.ui

import android.template.core.navigation.Navigator
import android.template.core.navigation.rememberNavigationState
import android.template.core.navigation.toEntries
import android.template.feature.mymodel.api.navigation.Main
import android.template.feature.mymodel.impl.navigation.MyModelEntryProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay

@Composable
fun MainNavigation() {

    // Single top-level destination for now; add more keys to topLevelKeys as the app grows.
    val navigationState = rememberNavigationState(startKey = Main, topLevelKeys = setOf(Main))
    val navigator = remember { Navigator(navigationState) }

    NavDisplay(
        entries = navigationState.toEntries(
            entryProvider = entryProvider {
                MyModelEntryProvider(navigator = navigator)
            }
        ),
        onBack = { navigator.goBack() },
    )
}
