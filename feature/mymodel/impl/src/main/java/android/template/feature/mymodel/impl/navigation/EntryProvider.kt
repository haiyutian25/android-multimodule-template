package android.template.feature.mymodel.impl.navigation

import android.template.core.navigation.Navigator
import android.template.feature.mymodel.api.navigation.Main
import android.template.feature.mymodel.impl.MyModelScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

@Composable
fun EntryProviderScope<NavKey>.MyModelEntryProvider(navigator: Navigator) {
    entry<Main> {
        MyModelScreen(
            onItemClick = { navKey -> navigator.navigate(navKey) },
            modifier = Modifier.padding(16.dp)
        )
    }
}
