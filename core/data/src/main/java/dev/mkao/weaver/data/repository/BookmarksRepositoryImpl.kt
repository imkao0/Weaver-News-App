package dev.mkao.weaver.data.repository

import dev.mkao.weaver.data.database.NewsDao
import dev.mkao.weaver.data.database.asDatabaseModel
import dev.mkao.weaver.data.database.asDomainModel
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.repository.BookmarksRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

@Singleton
class BookmarksRepositoryImpl @Inject constructor(
    private val newsDao: NewsDao,
) : BookmarksRepository {

    override fun getBookedArticlesStream(): Flow<List<Article>> =
        newsDao.getBookedArticles().map { it.asDomainModel() }

    override fun isArticleBookmarked(url: String): Flow<Boolean> =
        newsDao.isBookmarked(url)

    override suspend fun insertedArticle(article: Article) {
        newsDao.upsert(article.asDatabaseModel().copy(isBookedMarked = true))
    }

    override suspend fun deleteArticle(article: Article) {
        newsDao.setBookmark(article.url, false)
    }

    override suspend fun clearAllBookmarks() {
        newsDao.clearBookmarks()
    }

    override suspend fun getArticleByUrl(url: String): Article? =
        newsDao.getArticleByUrl(url).first()?.asDomainModel()
}
