package dev.mkao.weaver.features.home

import app.cash.turbine.test
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.data.repository.fakes.FakeRepository
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repository: FakeRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeRepository()
        preferencesRepository = mock()
        analyticsHelper = mock()

        // Default mock behaviors
        whenever(preferencesRepository.languageCode).thenReturn(flowOf("en"))
        whenever(preferencesRepository.countryCode).thenReturn(flowOf("us"))
        whenever(preferencesRepository.blockedSources).thenReturn(flowOf(emptySet()))
        whenever(preferencesRepository.preferredSources).thenReturn(flowOf(emptySet()))
        whenever(preferencesRepository.preferredLayout).thenReturn(flowOf("default"))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when articles are fetched, state updates correctly`() = runTest {
        val article = Article(
            url = "https://example.com",
            source = Source("id", "Name", "url"),
            author = "Author",
            title = "Title",
            content = "Content",
            description = "Description",
            isBookedMarked = false,
            image = null,
            publishedAt = "2026-01-01",
        )
        repository.setArticles(listOf(article))

        viewModel = HomeViewModel(repository, repository, preferencesRepository, analyticsHelper)

        viewModel.state.test {
            // UnconfinedTestDispatcher can skip intermediate synchronous
            // states; assert on the final state instead.
            val finalState = expectMostRecentItem()
            assertEquals(1, finalState.articles.size)
            assertEquals("Title", finalState.articles[0].title)
            assertEquals(false, finalState.isLoading)
        }
    }
}
