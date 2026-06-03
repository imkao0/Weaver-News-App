package dev.mkao.weaver.data.repository

import dev.mkao.weaver.data.database.DatabaseRecentSearch
import dev.mkao.weaver.data.database.RecentSearchDao
import dev.mkao.weaver.data.database.asDomainModel
import dev.mkao.weaver.data.network.NewsApi
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.domain.repository.SearchRepository
import dev.mkao.weaver.util.Result
import dev.mkao.weaver.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SearchRepositoryImpl @Inject constructor(
    private val newsApi: NewsApi,
    private val recentSearchDao: RecentSearchDao,
) : SearchRepository {

    override suspend fun searchRequest(query: String, language: String?): Result<List<Article>> {
        return when (
            val result = safeApiCall {
                newsApi.searchRequest(query = query, language = language)
            }
        ) {
            is Result.Success -> {
                Result.Success(result.data.articles.map { it.asDomainModel() })
            }
            is Result.NetworkError -> Result.Error(message = "No network connection")
            is Result.GenericError -> Result.Error(message = "Search failed: ${result.code}")
            is Result.Error -> Result.Error(message = result.message)
            is Result.Loading -> Result.Loading(true)
        }
    }

    override fun getRecentSearches(): Flow<List<RecentSearch>> =
        recentSearchDao.getRecentSearches().map { it.asDomainModel() }

    override suspend fun saveRecentSearch(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        recentSearchDao.getRecentSearchByQuery(trimmed)?.let { recentSearchDao.deleteByQuery(it.query) }
        recentSearchDao.insertRecentSearch(
            DatabaseRecentSearch(
                query = trimmed,
                searchTime = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun clearRecentSearches() {
        recentSearchDao.clearAll()
    }
}
