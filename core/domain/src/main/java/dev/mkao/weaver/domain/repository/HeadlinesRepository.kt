package dev.mkao.weaver.domain.repository

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.Flow

interface HeadlinesRepository {
    fun getArticlesStream(): Flow<List<Article>>

    /**
     * Image URL currently cached for [category], or null when the category
     * has never been fetched. Never triggers a network call.
     */
    suspend fun getCachedCategoryImage(category: String): String?

    /**
     * True when the category-thumbnail cache is stale and due for a periodic
     * refresh*/
    suspend fun shouldRefreshCategoryThumbnails(): Boolean

    /**
     * Marks the category-thumbnail cache as freshly refreshed at the current time.
     */
    suspend fun markCategoryThumbnailsRefreshed()

    /**
     * True when the background-refresh window (6 hours) has elapsed since the
     * last successful background sync, so the periodic worker should fetch.
     * Uses the [dev.mkao.weaver.data.database.TableUpdateInterval.BACKGROUND_REFRESH]
     * freshness interval.
     */
    suspend fun shouldRunBackgroundRefresh(): Boolean

    /** Marks the background-refresh ledger as freshly synced at the current time. */
    suspend fun markBackgroundRefreshed()

    suspend fun getTopHeadlines(country: String?, category: String, lang: String?): Result<List<Article>>
    suspend fun refreshTopHeadlines(country: String?, category: String, lang: String?)

    /**
     * Fetches a single [page] of headlines (1-based) for infinite-scroll
     * pagination. Unlike [getTopHeadlines], this bypasses the freshness
     * cache so scrolling always reaches the network, and reports the total
     * number of articles available on the backend via [PagedResult.totalArticles].
     */
    suspend fun getTopHeadlinesPage(
        country: String?,
        category: String,
        lang: String?,
        page: Int,
    ): Result<PagedResult>
}

/** Result of a paginated headlines request. */
data class PagedResult(
    val articles: List<Article>,
    val totalArticles: Int,
    val page: Int,
)
