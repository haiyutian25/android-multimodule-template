package android.template.feature.greeting.impl.navigation

import android.template.core.navigation.Navigator
import android.template.feature.greeting.api.navigation.Main
import android.template.feature.greeting.impl.GreetingScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

@Composable
fun EntryProviderScope<NavKey>.GreetingEntryProvider(navigator: Navigator) {
    entry<Main> {
        GreetingScreen(
            onItemClick = { navKey -> navigator.navigate(navKey) },
            modifier = Modifier.padding(16.dp)
        )
    }
}
