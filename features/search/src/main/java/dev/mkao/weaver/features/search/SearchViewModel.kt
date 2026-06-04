package dev.mkao.weaver.features.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.RecentSearch
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.domain.repository.SearchRepository
import dev.mkao.weaver.features.home.ArticleState
import dev.mkao.weaver.util.AppConstants
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
    private val headlinesRepository: HeadlinesRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleState())
    val state: StateFlow<ArticleState> = _state.asStateFlow()

    private val blockedSources = preferencesRepository.blockedSources
    private var searchJob: Job? = null

    val recentSearches: StateFlow<List<RecentSearch>> =
        repository.getRecentSearches()
            .stateIn(
                scope = viewModelScope,
                started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(AppConstants.STATE_FLOW_TIMEOUT_MS),
                initialValue = emptyList(),
            )

    init {
        preferencesRepository.languageCode.onEach { lang ->
            _state.update { it.copy(selectedLanguage = lang) }
        }.launchIn(viewModelScope)

        blockedSources.onEach { blocked ->
            _state.update { currentState ->
                currentState.copy(
                    articles = filterBlocked(currentState.articles, blocked),
                )
            }
        }.launchIn(viewModelScope)

        loadCachedArticles()
    }

    private fun loadCachedArticles() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val cachedArticles = headlinesRepository.getArticlesStream().first()
            val blocked = blockedSources.first()
            _state.update {
                it.copy(
                    articles = filterBlocked(cachedArticles, blocked),
                    isLoading = false,
                )
            }
        }
    }

    private fun filterBlocked(articles: List<Article>, blocked: Set<String>): List<Article> {
        return articles.filter { article ->
            val sourceName = article.source.name
            sourceName == null || sourceName !in blocked
        }
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
        if (query.length >= MIN_QUERY_LENGTH_FOR_ANALYTICS) {
            analyticsHelper.logEvent(AnalyticsEvent.SearchPerformed(query))
        }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            searchForNews(query = query)
        }
    }

    fun onSearchClicked() {
        _state.update { it.copy(isResultsVisible = true, articles = emptyList()) }
    }

    private fun searchForNews(query: String, saveToHistory: Boolean = true) {
        if (query.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val lang = preferencesRepository.languageCode.first()
            when (val result = repository.searchRequest(query, lang)) {
                is Result.Success -> {
                    if (saveToHistory) {
                        repository.saveRecentSearch(query)
                    }
                    val blocked = blockedSources.first()
                    _state.update {
                        it.copy(articles = filterBlocked(result.data, blocked), isLoading = false, error = null)
                    }
                }
                is Result.Error -> _state.update {
                    it.copy(error = result.message, isLoading = false, articles = emptyList())
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onRecentSearchClicked(query: String) {
        onSearchQueryChanged(query)
        onSearchClicked()
    }

    fun clearRecentSearches() {
        viewModelScope.launch { repository.clearRecentSearches() }
    }

    companion object {
        private const val SEARCH_DEBOUNCE_MS = 1000L
        private const val MIN_QUERY_LENGTH_FOR_ANALYTICS = 3
    }
}
