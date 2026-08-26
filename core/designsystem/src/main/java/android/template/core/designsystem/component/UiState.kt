package android.template.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A generic, reusable model for the UI state of a data-loading screen.
 */
sealed interface UiState<out T> {
    /** Data is being loaded. */
    data object Loading : UiState<Nothing>

    /** Loading failed. */
    data class Error(val throwable: Throwable) : UiState<Nothing>

    /** Loaded successfully but there is no data to show. */
    data object Empty : UiState<Nothing>

    /** Loaded successfully with data. */
    data class Success<T>(val data: T) : UiState<T>
}

/**
 * Renders the appropriate content for a [UiState]: a loading indicator while loading, an error
 * view with a retry action on failure, an empty view when there is no data, and [successContent]
 * when data is present.
 */
@Composable
fun <T> UiStateView(
    uiState: UiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    emptyContent: @Composable () -> Unit = { EmptyView(modifier = Modifier.fillMaxSize()) },
    successContent: @Composable (T) -> Unit,
) {
    Box(modifier = modifier) {
        when (uiState) {
            is UiState.Loading -> LoadingIndicator(modifier = Modifier.fillMaxSize())
            is UiState.Error -> ErrorView(
                message = uiState.throwable.message.orEmpty().ifEmpty { "Something went wrong" },
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )
            is UiState.Empty -> emptyContent()
            is UiState.Success -> successContent(uiState.data)
        }
    }
}
