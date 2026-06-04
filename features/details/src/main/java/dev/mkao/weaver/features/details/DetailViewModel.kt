package dev.mkao.weaver.features.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.features.details.DetailEvent
import dev.mkao.weaver.features.details.DetailState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: BookmarksRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state.asStateFlow()

    // One-shot UI events (share intents, etc.) delivered to the screen.
    private val _uiEvents = Channel<DetailUiEvent>(Channel.BUFFERED)
    val uiEvents = _uiEvents.receiveAsFlow()

    /**
     * Load an article by its key (Navigation 3 pattern). The article is
     * re-fetched from the Room SSOT so state survives process death / rotation.
     */
    fun loadArticle(articleUrl: String) {
        analyticsHelper.logEvent(AnalyticsEvent.ScreenView("article_detail"))
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val article = repository.getArticleByUrl(articleUrl)
            _state.update {
                it.copy(
                    selectedArticle = article,
                    isLoading = false,
                    error = if (article == null) "Article not found" else null,
                )
            }
        }
    }

    fun isBookmarked(url: String) = repository.isArticleBookmarked(url)

    fun onEvent(event: DetailEvent) {
        when (event) {
            is DetailEvent.ArticleSelected -> {
                _state.update { it.copy(selectedArticle = event.article) }
            }

            is DetailEvent.CategorySelected -> {
                _state.update { it.copy(selectedCategory = event.category) }
            }

            is DetailEvent.ToggleBookmark -> toggleBookmark(event.article)

            is DetailEvent.ShareArticle -> {
                viewModelScope.launch {
                    _uiEvents.send(DetailUiEvent.ShareArticle(event.article))
                }
            }

            is DetailEvent.BlockSource -> {
                viewModelScope.launch {
                    preferencesRepository.blockSource(event.sourceName)
                    analyticsHelper.logEvent(AnalyticsEvent.CategorySelected("blocked_${event.sourceName}"))
                }
            }
        }
    }

    private fun toggleBookmark(article: Article) {
        analyticsHelper.logEvent(
            AnalyticsEvent.BookmarkToggled(article.url, !article.isBookedMarked),
        )
        viewModelScope.launch {
            if (article.isBookedMarked) {
                repository.deleteArticle(article)
            } else {
                repository.insertedArticle(article)
            }
            // Reflect the toggle immediately on the open detail view.
            _state.value.selectedArticle?.let { selected ->
                if (selected.url == article.url) {
                    _state.update {
                        it.copy(selectedArticle = selected.copy(isBookedMarked = !article.isBookedMarked))
                    }
                }
            }
        }
    }
}

sealed class DetailUiEvent {
    data class ShareArticle(val article: Article) : DetailUiEvent()
}
