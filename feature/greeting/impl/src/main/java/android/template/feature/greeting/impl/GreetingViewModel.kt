package android.template.feature.greeting.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.template.core.data.util.SyncManager
import android.template.core.designsystem.component.UiState
import android.template.core.domain.AddGreetingUseCase
import android.template.core.domain.GetGreetingsUseCase
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when flatMapLatest is stable
class GreetingViewModel @Inject constructor(
    private val getGreetings: GetGreetingsUseCase,
    private val addGreeting: AddGreetingUseCase,
    syncManager: SyncManager,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<UiState<List<String>>> = retryTrigger
        .flatMapLatest {
            getGreetings()
                .map<List<String>, UiState<List<String>>> { data ->
                    if (data.isEmpty()) UiState.Empty else UiState.Success(data)
                }
                .catch { emit(UiState.Error(it)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addGreeting(message: String) {
        viewModelScope.launch {
            addGreeting(message)
        }
    }

    fun retry() {
        retryTrigger.value++
    }
}
