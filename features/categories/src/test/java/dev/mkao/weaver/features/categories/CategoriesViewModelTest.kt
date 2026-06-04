package dev.mkao.weaver.features.categories

import app.cash.turbine.test
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.data.repository.fakes.FakeRepository
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class CategoriesViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: CategoriesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRepository()
        preferencesRepository = mock()
        analyticsHelper = mock()

        whenever(preferencesRepository.preferredTopics).thenReturn(flowOf(emptySet()))
        whenever(preferencesRepository.preferredSources).thenReturn(flowOf(emptySet()))

        viewModel = CategoriesViewModel(repository, preferencesRepository, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when category is toggled, it is saved to preferences including default general`() = runTest {
        viewModel.onCategoryToggled("sports")

        // Initial state has "general" because preferredTopics was empty.
        // Toggling "sports" adds it to the set.
        verify(preferencesRepository).setPreferredTopics(setOf("general", "sports"))
    }

    @Test
    fun `when source is toggled, it is saved to preferences`() = runTest {
        viewModel.onSourceToggled("bbc-news")

        verify(preferencesRepository).setPreferredSources(setOf("bbc-news"))
    }

    @Test
    fun `when state is observed, it reflects followed sources`() = runTest {
        whenever(preferencesRepository.preferredSources).thenReturn(flowOf(setOf("cnn")))

        // Re-init to pick up the mock flow
        viewModel = CategoriesViewModel(repository, preferencesRepository, analyticsHelper)

        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state.followedSources.contains("cnn"))
        }
    }
}
