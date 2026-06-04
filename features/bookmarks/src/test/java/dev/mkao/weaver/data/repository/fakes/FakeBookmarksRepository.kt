package dev.mkao.weaver.data.repository.fakes

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.repository.BookmarksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeBookmarksRepository : BookmarksRepository {
    private val _bookmarks = MutableStateFlow<Map<String, Article>>(emptyMap())

    override fun getBookedArticlesStream(): Flow<List<Article>> = _bookmarks.map { it.values.toList() }

    override fun isArticleBookmarked(url: String): Flow<Boolean> = _bookmarks.map { it.containsKey(url) }

    override suspend fun insertedArticle(article: Article) {
        _bookmarks.value = _bookmarks.value + (article.url to article.copy(isBookedMarked = true))
    }

    override suspend fun deleteArticle(article: Article) {
        _bookmarks.value = _bookmarks.value - article.url
    }

    override suspend fun clearAllBookmarks() {
        _bookmarks.value = emptyMap()
    }

    override suspend fun getArticleByUrl(url: String): Article? = _bookmarks.value[url]
}
