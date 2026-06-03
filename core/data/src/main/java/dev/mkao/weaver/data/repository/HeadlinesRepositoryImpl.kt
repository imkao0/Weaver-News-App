package dev.mkao.weaver.data.repository

import dev.mkao.weaver.data.database.DatabaseArticle
import dev.mkao.weaver.data.database.DatabaseTableUpdate
import dev.mkao.weaver.data.database.NewsDao
import dev.mkao.weaver.data.database.TableUpdateDao
import dev.mkao.weaver.data.database.TableUpdateInterval
import dev.mkao.weaver.data.database.asDatabaseModel
import dev.mkao.weaver.data.database.asDomainModel
import dev.mkao.weaver.data.network.NewsApi
import dev.mkao.weaver.data.network.model.NetworkNewsResponse
import dev.mkao.weaver.domain.analytics.CrashlyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.domain.repository.PagedResult
import dev.mkao.weaver.util.NewsCategories
import dev.mkao.weaver.util.Result
import dev.mkao.weaver.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

@Singleton
class HeadlinesRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val newsDao: NewsDao,
    private val crashlyticsHelper: CrashlyticsHelper,
    private val tableUpdateDao: TableUpdateDao,
) : HeadlinesRepository {

    private val cacheMutex = Mutex()

    override suspend fun getCachedCategoryImage(category: String): String? =
        newsDao.getLatestImageForCategory(category)

    override suspend fun shouldRefreshCategoryThumbnails(): Boolean =
        !isFresh(
            CATEGORY_THUMBNAILS_KEY,
            TableUpdateInterval.CATEGORY_THUMBNAILS.intervalMillis,
        )

    override suspend fun markCategoryThumbnailsRefreshed() =
        recordFreshness(CATEGORY_THUMBNAILS_KEY)

    override suspend fun shouldRunBackgroundRefresh(): Boolean =
        !isFresh(
            BACKGROUND_REFRESH_KEY,
            TableUpdateInterval.BACKGROUND_REFRESH.intervalMillis,
        )

    override suspend fun markBackgroundRefreshed() =
        recordFreshness(BACKGROUND_REFRESH_KEY)

    override fun getArticlesStream(): Flow<List<Article>> =
        newsDao.getArticles().map { it.asDomainModel() }

    override suspend fun getTopHeadlines(
        country: String?,
        category: String,
        lang: String?,
    ): Result<List<Article>> {
        return when (
            val result = safeApiCall {
                fetchTopHeadlines(country, category, lang)
            }
        ) {
            is Result.Success -> {
                val network = result.data.articles
                cachePreservingBookmarks(network.map { it.asDatabaseModel(category) })
                recordFreshness(freshnessKey(country, category))
                Result.Success(network.map { it.asDomainModel() })
            }
            is Result.NetworkError, is Result.GenericError, is Result.Error -> {
                val message = "Headlines refresh failed: ${result.toErrorMessage()}"
                Timber.tag(TAG).w("$message Serving cache.")
                runCatching { crashlyticsHelper.log(message) }
                val cachedEntities = if (category == "general" || category.isBlank()) {
                    newsDao.getArticles().first()
                } else {
                    newsDao.getArticlesByCategory(category).first()
                }
                val cached = cachedEntities.asDomainModel()
                if (cached.isNotEmpty()) {
                    Result.Success(cached)
                } else {
                    Result.Error(message = message)
                }
            }
            is Result.Loading -> Result.Loading(true)
        }
    }

    override suspend fun refreshTopHeadlines(country: String?, category: String, lang: String?) {
        when (
            val result = safeApiCall {
                fetchTopHeadlines(country, category, lang)
            }
        ) {
            is Result.Success ->
                cachePreservingBookmarks(result.data.articles.map { it.asDatabaseModel(category) })
            else -> Timber.tag(TAG).w("refreshTopHeadlines failed; keeping existing cache")
        }
    }

    private suspend fun fetchTopHeadlines(
        country: String?,
        category: String,
        lang: String?,
        page: Int = NewsApi.FIRST_PAGE,
        enforceFreshness: Boolean = true,
    ): NetworkNewsResponse {
        val freshnessKey = freshnessKey(country, category)
        if (enforceFreshness &&
            page == NewsApi.FIRST_PAGE &&
            isFresh(freshnessKey, TableUpdateInterval.TOP_STORIES.intervalMillis)
        ) {
            Timber.tag(TAG).d("Feed $freshnessKey is fresh; serving cache")
            throw FreshCacheException()
        }
        val editionCountry = country?.takeUnless { it.isBlank() }?.lowercase()
            ?: DEFAULT_COUNTRY
        return if (category.contains(",")) {
            val query = category.split(",").joinToString(" OR ") { it.trim() }
            newsApi.searchRequest(
                query = query,
                language = lang ?: "en",
                country = editionCountry,
                page = page,
            )
        } else {
            val mappedCategory = when (category.lowercase()) {
                "top" -> "general"
                else -> category.lowercase()
            }
            if (NewsCategories.isNativeApiCategory(mappedCategory)) {
                newsApi.getTopHeadlines(
                    country = editionCountry,
                    category = mappedCategory,
                    language = lang ?: "en",
                    page = page,
                )
            } else {
                newsApi.searchRequest(
                    query = mappedCategory,
                    language = lang ?: "en",
                    country = editionCountry,
                    page = page,
                )
            }
        }
    }

    override suspend fun getTopHeadlinesPage(
        country: String?,
        category: String,
        lang: String?,
        page: Int,
    ): Result<PagedResult> {
        return when (
            val result = safeApiCall {
                fetchTopHeadlines(country, category, lang, page = page, enforceFreshness = false)
            }
        ) {
            is Result.Success -> {
                val response = result.data
                // Page 1 replaces the cached feed; subsequent pages append to it
                // so bookmarked/cached articles stay consistent.
                cachePreservingBookmarks(response.articles.map { it.asDatabaseModel(category) })
                if (page == NewsApi.FIRST_PAGE) {
                    recordFreshness(freshnessKey(country, category))
                }
                Result.Success(
                    PagedResult(
                        articles = response.articles.map { it.asDomainModel() },
                        totalArticles = response.totalArticles,
                        page = page,
                    ),
                )
            }
            is Result.NetworkError, is Result.GenericError, is Result.Error -> {
                val message = "Headlines page $page failed: ${result.toErrorMessage()}"
                Timber.tag(TAG).w(message)
                runCatching { crashlyticsHelper.log(message) }
                Result.Error(message = result.toErrorMessage())
            }
            is Result.Loading -> Result.Loading(true)
        }
    }

    private suspend fun cachePreservingBookmarks(articles: List<DatabaseArticle>) =
        cacheMutex.withLock {
            val bookmarkedUrls = newsDao.getBookmarkedUrls().toSet()
            val merged = articles.map { article ->
                if (article.url in bookmarkedUrls) article.copy(isBookedMarked = true) else article
            }
            newsDao.upsertAll(merged)
        }

    private fun freshnessKey(country: String?, category: String): String {
        val editionCountry = country?.takeUnless { it.isBlank() }?.lowercase()
            ?: DEFAULT_COUNTRY
        return "top_stories_${category.lowercase()}_$editionCountry"
    }

    private suspend fun isFresh(key: String, intervalMillis: Long): Boolean {
        val last = tableUpdateDao.getTableLastUpdateTime(key)?.last_updated ?: return false
        return System.currentTimeMillis() - last < intervalMillis
    }

    private suspend fun recordFreshness(key: String) {
        tableUpdateDao.insertTableUpdateLog(
            DatabaseTableUpdate(
                table_name = key,
                last_updated = System.currentTimeMillis(),
            ),
        )
    }

    private class FreshCacheException : Exception("feed cache is fresh")

    companion object {
        private const val TAG = "HeadlinesRepo"
        private const val DEFAULT_COUNTRY = "us"
        private const val CATEGORY_THUMBNAILS_KEY = "category_thumbnails"
        private const val BACKGROUND_REFRESH_KEY = "background_refresh"
    }
}

private fun Result<*>.toErrorMessage(): String = when (this) {
    is Result.GenericError -> {
        val apiMessage = error?.message
        when (code) {
            400 -> apiMessage ?: "Bad request"
            401 -> apiMessage ?: "Invalid API key"
            403 -> apiMessage ?: "Daily API quota reached"
            429 -> apiMessage ?: "Too many requests"
            else -> apiMessage ?: "API error ($code)"
        }
    }
    is Result.NetworkError -> "No network connection"
    is Result.Error -> message ?: "Unexpected error"
    else -> "Unknown error"
}
