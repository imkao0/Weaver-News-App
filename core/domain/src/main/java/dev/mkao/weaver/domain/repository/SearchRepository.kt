package dev.mkao.weaver.domain.repository

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    suspend fun searchRequest(query: String, language: String?): Result<List<Article>>
    fun getRecentSearches(): Flow<List<RecentSearch>>
    suspend fun saveRecentSearch(query: String)
    suspend fun clearRecentSearches()
}
