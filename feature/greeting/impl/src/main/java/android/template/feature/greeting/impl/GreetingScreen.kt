package android.template.feature.greeting.impl

import android.template.core.designsystem.component.UiStateView
import android.template.core.designsystem.theme.AppTheme
import android.template.core.designsystem.theme.Spacing
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey

@Composable
fun GreetingScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GreetingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    Column(
        modifier = modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        if (isSyncing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        GreetingInput(onSave = viewModel::addGreeting)
        UiStateView(
            uiState = uiState,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            successContent = { items -> GreetingList(items = items) }
        )
    }
}

@Composable
private fun GreetingInput(
    onSave: (message: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf("Hello World") }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = Spacing.l),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = message,
            onValueChange = { message = it }
        )

        Button(modifier = Modifier.width(96.dp), onClick = { onSave(message) }) {
            Text(stringResource(R.string.save_label))
        }
    }
}

@Composable
private fun GreetingList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            Text(text = stringResource(R.string.saved_item_format, item))
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun GreetingInputPreview() {
    AppTheme {
        GreetingInput(onSave = {})
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun GreetingListPreview() {
    AppTheme {
        GreetingList(items = listOf("Compose", "Room", "Kotlin"))
    }
}
