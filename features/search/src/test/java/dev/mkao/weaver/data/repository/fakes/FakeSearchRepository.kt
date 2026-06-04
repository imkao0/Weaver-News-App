package dev.mkao.weaver.data.repository.fakes

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.domain.repository.SearchRepository
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSearchRepository : SearchRepository {

    private val _recentSearches = MutableStateFlow<List<RecentSearch>>(emptyList())
    var searchResult: Result<List<Article>> = Result.Success(emptyList())

    override suspend fun searchRequest(query: String, language: String?): Result<List<Article>> {
        return searchResult
    }

    override fun getRecentSearches(): Flow<List<RecentSearch>> = _recentSearches.asStateFlow()

    override suspend fun saveRecentSearch(query: String) {
        val current = _recentSearches.value
        val newSearch = RecentSearch(
            id = (current.size + 1),
            query = query,
            searchTime = System.currentTimeMillis(),
        )
        _recentSearches.value = current + newSearch
    }

    override suspend fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }
}
