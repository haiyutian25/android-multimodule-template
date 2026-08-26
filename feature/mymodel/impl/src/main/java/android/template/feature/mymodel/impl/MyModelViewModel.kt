package android.template.feature.mymodel.impl

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
import android.template.core.domain.AddMyModelUseCase
import android.template.core.domain.GetMyModelsUseCase
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when flatMapLatest is stable
class MyModelViewModel @Inject constructor(
    private val getMyModels: GetMyModelsUseCase,
    private val addMyModel: AddMyModelUseCase,
    syncManager: SyncManager,
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<UiState<List<String>>> = retryTrigger
        .flatMapLatest {
            getMyModels()
                .map<List<String>, UiState<List<String>>> { data ->
                    if (data.isEmpty()) UiState.Empty else UiState.Success(data)
                }
                .catch { emit(UiState.Error(it)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState.Loading)

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addMyModel(name: String) {
        viewModelScope.launch {
            addMyModel(name)
        }
    }

    fun retry() {
        retryTrigger.value++
    }
}
