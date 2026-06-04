package dev.mkao.weaver.data.repository.fakes

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.domain.repository.PagedResult
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeHeadlinesRepository : HeadlinesRepository {
    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    var topHeadlinesResult: Result<List<Article>> = Result.Success(emptyList())
    var pagedResult: Result<PagedResult>? = null
    var cachedCategoryImage: String? = null
    var shouldRefreshCategoryThumbnails = true

    override fun getArticlesStream(): Flow<List<Article>> = _articles

    override suspend fun getCachedCategoryImage(category: String): String? = cachedCategoryImage

    override suspend fun shouldRefreshCategoryThumbnails(): Boolean = shouldRefreshCategoryThumbnails

    override suspend fun markCategoryThumbnailsRefreshed() = Unit

    var shouldRunBackgroundRefresh = true

    override suspend fun shouldRunBackgroundRefresh(): Boolean = shouldRunBackgroundRefresh

    override suspend fun markBackgroundRefreshed() = Unit

    override suspend fun getTopHeadlines(
        country: String?,
        category: String,
        lang: String?,
    ): Result<List<Article>> = topHeadlinesResult

    override suspend fun refreshTopHeadlines(country: String?, category: String, lang: String?) {
        if (topHeadlinesResult is Result.Success) {
            _articles.value = (topHeadlinesResult as Result.Success).data
        }
    }

    override suspend fun getTopHeadlinesPage(
        country: String?,
        category: String,
        lang: String?,
        page: Int,
    ): Result<PagedResult> = pagedResult ?: when (val result = topHeadlinesResult) {
        is Result.Success -> Result.Success(
            PagedResult(
                articles = result.data,
                totalArticles = result.data.size,
                page = page,
            ),
        )
        is Result.Error -> Result.Error(result.exception, result.message)
        is Result.GenericError -> Result.GenericError(result.code, result.error, result.exception)
        is Result.NetworkError -> Result.NetworkError(result.exception)
        is Result.Loading -> Result.Loading(true)
    }
}
