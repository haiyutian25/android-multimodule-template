package android.template.feature.mymodel.impl


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import android.template.core.domain.AddMyModelUseCase
import android.template.core.domain.GetMyModelsUseCase
import android.template.core.testing.repository.TestMyModelRepository
import android.template.core.testing.util.MainDispatcherRule
import android.template.core.testing.util.TestSyncManager
import android.template.feature.mymodel.impl.MyModelUiState
import android.template.feature.mymodel.impl.MyModelViewModel

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class MyModelViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestMyModelRepository()
    private lateinit var viewModel: MyModelViewModel

    @Before
    fun setup() {
        viewModel = MyModelViewModel(
            GetMyModelsUseCase(repository),
            AddMyModelUseCase(repository),
            TestSyncManager(),
        )
    }

    @Test
    fun uiState_initiallyLoading() = runTest {
        assertEquals(MyModelUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_whenModelsEmitted_isSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        repository.sendMyModels(listOf("One", "Two"))

        assertEquals(
            MyModelUiState.Success(listOf("One", "Two")),
            viewModel.uiState.value,
        )
    }
}
