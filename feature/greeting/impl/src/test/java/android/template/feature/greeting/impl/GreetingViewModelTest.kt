package android.template.feature.greeting.impl


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import android.template.core.domain.AddGreetingUseCase
import android.template.core.domain.GetGreetingsUseCase
import android.template.core.testing.repository.TestGreetingRepository
import android.template.core.testing.util.MainDispatcherRule
import android.template.core.testing.util.TestSyncManager
import android.template.core.designsystem.component.UiState
import android.template.feature.greeting.impl.GreetingViewModel

/**
 * To learn more about how this test handles Flows created with stateIn, see
 * https://developer.android.com/kotlin/flow/test#statein
 */
@OptIn(ExperimentalCoroutinesApi::class) // TODO: Remove when stable
class GreetingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = TestGreetingRepository()
    private lateinit var viewModel: GreetingViewModel

    @Before
    fun setup() {
        viewModel = GreetingViewModel(
            GetGreetingsUseCase(repository),
            AddGreetingUseCase(repository),
            TestSyncManager(),
        )
    }

    @Test
    fun uiState_initiallyLoading() = runTest {
        assertEquals(UiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun uiState_whenModelsEmitted_isSuccess() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

        repository.sendGreetings(listOf("One", "Two"))

        assertEquals(
            UiState.Success(listOf("One", "Two")),
            viewModel.uiState.value,
        )
    }
}
