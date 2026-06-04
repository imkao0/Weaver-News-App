package dev.mkao.weaver.data.repository.fakes

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.domain.repository.PagedResult
import dev.mkao.weaver.domain.repository.ArticleRepository
import dev.mkao.weaver.domain.repository.SearchRepository
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory repository fake for fast, deterministic unit tests.
 */
class FakeRepository : ArticleRepository, HeadlinesRepository, SearchRepository, BookmarksRepository {

    private val articlesState = MutableStateFlow<List<Article>>(emptyList())
    private val bookmarkedUrls = MutableStateFlow<Set<String>>(emptySet())
    private val recentSearches = MutableStateFlow<List<RecentSearch>>(emptyList())

    fun setArticles(articles: List<Article>) {
        articlesState.value = articles
    }

    override fun getArticlesStream(): Flow<List<Article>> = articlesState

    override suspend fun getCachedCategoryImage(category: String): String? =
        articlesState.value
            .lastOrNull { it.image != null }
            ?.image

    override suspend fun shouldRefreshCategoryThumbnails(): Boolean = false

    override suspend fun markCategoryThumbnailsRefreshed() = Unit

    override suspend fun shouldRunBackgroundRefresh(): Boolean = false

    override suspend fun markBackgroundRefreshed() = Unit

    override fun getBookedArticlesStream(): Flow<List<Article>> =
        articlesState.map { list -> list.filter { it.url in bookmarkedUrls.value || it.isBookedMarked } }

    override fun isArticleBookmarked(url: String): Flow<Boolean> =
        bookmarkedUrls.map { it.contains(url) }

    override suspend fun getTopHeadlines(
        country: String?,
        category: String,
        lang: String?,
    ): Result<List<Article>> = Result.Success(articlesState.value)

    override suspend fun getTopHeadlinesPage(
        country: String?,
        category: String,
        lang: String?,
        page: Int,
    ): Result<PagedResult> = Result.Success(
        PagedResult(
            articles = articlesState.value,
            totalArticles = articlesState.value.size,
            page = page,
        ),
    )

    override suspend fun searchRequest(query: String, language: String?): Result<List<Article>> =
        Result.Success(articlesState.value.filter { it.title.contains(query, ignoreCase = true) })

    override suspend fun refreshTopHeadlines(country: String?, category: String, lang: String?) = Unit

    override suspend fun insertedArticle(article: Article) {
        bookmarkedUrls.value += article.url
    }

    override suspend fun deleteArticle(article: Article) {
        bookmarkedUrls.value -= article.url
    }

    override suspend fun clearAllBookmarks() {
        bookmarkedUrls.value = emptySet()
        articlesState.value = articlesState.value.map { it.copy(isBookedMarked = false) }
    }

    override suspend fun getArticleByUrl(url: String): Article? =
        articlesState.value.find { it.url == url }

    override fun getRecentSearches(): Flow<List<RecentSearch>> = recentSearches

    override suspend fun saveRecentSearch(query: String) {
        val nextId = (recentSearches.value.maxOfOrNull { it.id } ?: 0) + 1
        recentSearches.value += RecentSearch(nextId, query, System.currentTimeMillis())
    }

    override suspend fun clearRecentSearches() {
        recentSearches.value = emptyList()
    }
}
