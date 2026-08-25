package android.template.feature.mymodel.impl.navigation

import android.template.feature.mymodel.api.navigation.Main
import android.template.feature.mymodel.impl.MyModelScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

@Composable
fun EntryProviderScope<NavKey>.MyModelEntryProvider(backStack: NavBackStack<NavKey>) {
    entry<Main> {
        MyModelScreen(
            onItemClick = { navKey -> backStack.add(navKey) },
            modifier = Modifier.padding(16.dp)
        )
    }
}
