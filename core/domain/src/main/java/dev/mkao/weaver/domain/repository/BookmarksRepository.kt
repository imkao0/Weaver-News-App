package dev.mkao.weaver.domain.repository

import dev.mkao.weaver.domain.model.Article
import kotlinx.coroutines.flow.Flow

interface BookmarksRepository {
    fun getBookedArticlesStream(): Flow<List<Article>>
    fun isArticleBookmarked(url: String): Flow<Boolean>
    suspend fun insertedArticle(article: Article)
    suspend fun deleteArticle(article: Article)
    suspend fun clearAllBookmarks()
    suspend fun getArticleByUrl(url: String): Article?
}
