package dev.mkao.weaver.features.search

import app.cash.turbine.test
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.data.repository.fakes.FakeHeadlinesRepository
import dev.mkao.weaver.data.repository.fakes.FakeSearchRepository
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.Source
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var searchRepository: FakeSearchRepository
    private lateinit var headlinesRepository: FakeHeadlinesRepository
    private lateinit var preferencesRepository: UserPreferencesRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        searchRepository = FakeSearchRepository()
        headlinesRepository = FakeHeadlinesRepository()
        preferencesRepository = mock()
        analyticsHelper = mock()

        whenever(preferencesRepository.languageCode).thenReturn(flowOf("en"))
        whenever(preferencesRepository.blockedSources).thenReturn(flowOf(emptySet()))

        viewModel = SearchViewModel(searchRepository, headlinesRepository, preferencesRepository, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when search query changes, results are updated after debounce`() = runTest {
        val article = Article(
            url = "https://example.com",
            source = Source("id", "Name", "url"),
            author = "Author",
            title = "Search Results",
            content = "Content",
            description = "Description",
            isBookedMarked = false,
            image = null,
            publishedAt = "2026-01-01",
        )
        searchRepository.searchResult = Result.Success(listOf(article))

        viewModel.onSearchQueryChanged("Search")

        // Advance time to bypass SEARCH_DEBOUNCE_MS (1000ms)
        advanceTimeBy(1001)

        viewModel.state.test {
            val state = awaitItem()
            assertEquals(1, state.articles.size)
            assertEquals("Search Results", state.articles[0].title)
        }
    }

    @Test
    fun `when search is performed, query is saved to recent searches`() = runTest {
        viewModel.onSearchQueryChanged("New Query")

        advanceTimeBy(1001)

        searchRepository.getRecentSearches().test {
            val history = awaitItem()
            assertEquals(1, history.size)
            assertEquals("New Query", history[0].query)
        }
    }

    @Test
    fun `when recent searches are cleared, state reflects empty history`() = runTest {
        searchRepository.saveRecentSearch("Query 1")
        searchRepository.saveRecentSearch("Query 2")

        viewModel.clearRecentSearches()

        viewModel.recentSearches.test {
            val history = awaitItem()
            assertEquals(0, history.size)
        }
    }
}
