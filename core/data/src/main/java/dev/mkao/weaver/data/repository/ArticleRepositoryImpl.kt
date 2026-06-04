package dev.mkao.weaver.data.repository

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.domain.repository.ArticleRepository
import dev.mkao.weaver.domain.repository.SearchRepository
import dev.mkao.weaver.util.Result
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Modern implementation that delegates to the new focused repositories.
 */
@Singleton
class ArticleRepositoryImpl @Inject constructor(
    private val headlinesRepo: HeadlinesRepository,
    private val searchRepo: SearchRepository,
    private val bookmarksRepo: BookmarksRepository,
) : ArticleRepository {

    override fun getArticlesStream(): Flow<List<Article>> =
        headlinesRepo.getArticlesStream()

    override fun getBookedArticlesStream(): Flow<List<Article>> =
        bookmarksRepo.getBookedArticlesStream()

    override fun isArticleBookmarked(url: String): Flow<Boolean> =
        bookmarksRepo.isArticleBookmarked(url)

    override suspend fun getTopHeadlines(
        country: String?,
        category: String,
        lang: String?,
    ): Result<List<Article>> = headlinesRepo.getTopHeadlines(country, category, lang)

    override suspend fun searchRequest(query: String, language: String?): Result<List<Article>> =
        searchRepo.searchRequest(query, language)

    override suspend fun refreshTopHeadlines(country: String?, category: String, lang: String?) =
        headlinesRepo.refreshTopHeadlines(country, category, lang)

    override suspend fun insertedArticle(article: Article) =
        bookmarksRepo.insertedArticle(article)

    override suspend fun deleteArticle(article: Article) =
        bookmarksRepo.deleteArticle(article)

    override suspend fun getArticleByUrl(url: String): Article? =
        bookmarksRepo.getArticleByUrl(url)

    override fun getRecentSearches(): Flow<List<RecentSearch>> =
        searchRepo.getRecentSearches()

    override suspend fun saveRecentSearch(query: String) =
        searchRepo.saveRecentSearch(query)

    override suspend fun clearRecentSearches() =
        searchRepo.clearRecentSearches()
}
