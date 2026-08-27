package android.template.feature.greeting.impl

import android.template.core.designsystem.theme.AppTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavKey

/**
 * Entry screen of the template.
 *
 * It intentionally shows nothing but a "Hello World" label: the template ships without any
 * business UI. The full layered architecture (ViewModel, use cases, repository, data sources,
 * database, sync) is still present underneath and can be wired to this screen - see the
 * development manual for how to build a feature UI on top of it.
 */
@Composable
fun GreetingScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .safeDrawingPadding()
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = stringResource(R.string.hello_world))
    }
}

@Preview(showBackground = true)
@Composable
private fun GreetingScreenPreview() {
    AppTheme {
        GreetingScreen(onItemClick = {})
    }
}
