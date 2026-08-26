package android.template.feature.mymodel.impl

import android.template.core.designsystem.component.UiStateView
import android.template.core.designsystem.theme.MyApplicationTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey

@Composable
fun MyModelScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MyModelViewModel = hiltViewModel()
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
        MyModelInput(onSave = viewModel::addMyModel)
        UiStateView(
            uiState = uiState,
            onRetry = viewModel::retry,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            successContent = { items -> MyModelList(items = items) }
        )
    }
}

@Composable
private fun MyModelInput(
    onSave: (name: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var nameMyModel by remember { mutableStateOf("Compose") }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextField(
            modifier = Modifier.weight(1f),
            value = nameMyModel,
            onValueChange = { nameMyModel = it }
        )

        Button(modifier = Modifier.width(96.dp), onClick = { onSave(nameMyModel) }) {
            Text("Save")
        }
    }
}

@Composable
private fun MyModelList(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        items.forEach {
            Text("Saved item: $it")
        }
    }
}

// Previews

@Preview(showBackground = true)
@Composable
private fun MyModelInputPreview() {
    MyApplicationTheme {
        MyModelInput(onSave = {})
    }
}

@Preview(showBackground = true, widthDp = 340)
@Composable
private fun MyModelListPreview() {
    MyApplicationTheme {
        MyModelList(items = listOf("Compose", "Room", "Kotlin"))
    }
}
