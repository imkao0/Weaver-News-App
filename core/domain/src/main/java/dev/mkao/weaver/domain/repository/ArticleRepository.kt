package dev.mkao.weaver.domain.repository

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.Flow

interface ArticleRepository {

    /** Reactive stream of all cached articles (the local SSOT). */
    fun getArticlesStream(): Flow<List<Article>>

    /** Reactive stream of bookmarked articles. */
    fun getBookedArticlesStream(): Flow<List<Article>>

    /** Reactive bookmark state for a single article. */
    fun isArticleBookmarked(url: String): Flow<Boolean>

    /** Network-first refresh; falls back to the cached articles on failure. */
    suspend fun getTopHeadlines(
        country: String?,
        category: String,
        lang: String?,
    ): Result<List<Article>>

    suspend fun searchRequest(query: String, language: String? = null): Result<List<Article>>

    suspend fun refreshTopHeadlines(country: String?, category: String, lang: String?)

    suspend fun insertedArticle(article: Article)

    suspend fun deleteArticle(article: Article)

    suspend fun getArticleByUrl(url: String): Article?

    fun getRecentSearches(): Flow<List<RecentSearch>>

    suspend fun saveRecentSearch(query: String)

    suspend fun clearRecentSearches()
}
