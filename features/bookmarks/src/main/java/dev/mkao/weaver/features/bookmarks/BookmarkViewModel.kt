package dev.mkao.weaver.features.bookmarks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.util.AppConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val repository: BookmarksRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    val bookmarkedArticles: StateFlow<List<Article>> = repository.getBookedArticlesStream()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS),
            initialValue = emptyList(),
        )

    fun toggleBookmark(article: Article) {
        analyticsHelper.logEvent(
            AnalyticsEvent.BookmarkToggled(article.url, !article.isBookedMarked),
        )
        viewModelScope.launch {
            if (article.isBookedMarked) {
                repository.deleteArticle(article)
            } else {
                repository.insertedArticle(article)
            }
        }
    }

    fun clearAllBookmarks() {
        analyticsHelper.logEvent(AnalyticsEvent.BookmarksCleared)
        viewModelScope.launch {
            repository.clearAllBookmarks()
        }
    }

    fun isArticleBookmarked(url: String): Flow<Boolean> = repository.isArticleBookmarked(url)
}
