package android.template.feature.mymodel.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.template.core.data.util.SyncManager
import android.template.core.domain.AddMyModelUseCase
import android.template.core.domain.GetMyModelsUseCase
import android.template.feature.mymodel.impl.MyModelUiState.Error
import android.template.feature.mymodel.impl.MyModelUiState.Loading
import android.template.feature.mymodel.impl.MyModelUiState.Success
import javax.inject.Inject

@HiltViewModel
class MyModelViewModel @Inject constructor(
    getMyModels: GetMyModelsUseCase,
    private val addMyModel: AddMyModelUseCase,
    syncManager: SyncManager,
) : ViewModel() {

    val uiState: StateFlow<MyModelUiState> = getMyModels()
        .map<List<String>, MyModelUiState> { Success(data = it) }
        .catch { emit(Error(it)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Loading)

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun addMyModel(name: String) {
        viewModelScope.launch {
            addMyModel(name)
        }
    }
}

sealed interface MyModelUiState {
    object Loading : MyModelUiState
    data class Error(val throwable: Throwable) : MyModelUiState
    data class Success(val data: List<String>) : MyModelUiState
}
