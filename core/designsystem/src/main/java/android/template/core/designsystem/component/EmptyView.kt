package android.template.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import android.template.core.designsystem.theme.AppTheme

/**
 * Displays an empty-state message.
 */
@Composable
fun EmptyView(
    modifier: Modifier = Modifier,
    message: String = "No data",
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
private fun EmptyViewPreview() {
    AppTheme {
        EmptyView()
    }
}
