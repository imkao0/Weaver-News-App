package dev.mkao.weaver.features.bookmarks

import app.cash.turbine.test
import dev.mkao.weaver.data.repository.fakes.FakeBookmarksRepository
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

@OptIn(ExperimentalCoroutinesApi::class)
class BookmarkViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: FakeBookmarksRepository
    private lateinit var analyticsHelper: AnalyticsHelper
    private lateinit var viewModel: BookmarkViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBookmarksRepository()
        analyticsHelper = mock()
        viewModel = BookmarkViewModel(repository, analyticsHelper)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun sampleArticle() = Article(
        url = "https://example.com",
        source = Source("id", "Name", "https://source.com"),
        author = "Author",
        title = "Title",
        content = "Content",
        description = "Description",
        isBookedMarked = true,
        image = null,
        publishedAt = "2026-01-01",
    )

    @Test
    fun `when repository has bookmarked articles, state updates correctly`() = runTest {
        val article = sampleArticle()

        viewModel.bookmarkedArticles.test {
            // Skip initial value (empty list)
            assertEquals(emptyList<Article>(), awaitItem())

            repository.insertedArticle(article)

            val item = awaitItem()
            assertEquals(1, item.size)
            assertEquals("Title", item[0].title)
        }
    }

    @Test
    fun `clearAllBookmarks empties the bookmarked articles state`() = runTest {
        val article = sampleArticle()

        viewModel.bookmarkedArticles.test {
            assertEquals(emptyList<Article>(), awaitItem())

            repository.insertedArticle(article)
            assertEquals(1, awaitItem().size)

            repository.clearAllBookmarks()
            assertEquals(emptyList<Article>(), awaitItem())
        }
    }
}
