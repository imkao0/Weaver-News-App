package dev.mkao.weaver.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.Edition
import dev.mkao.weaver.domain.repository.BookmarksRepository
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.features.home.ArticleEvent
import dev.mkao.weaver.features.home.ArticleState
import dev.mkao.weaver.features.home.ArticleUiEvent
import dev.mkao.weaver.features.shared.LanguageConstants
import dev.mkao.weaver.util.NewsSources
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HeadlinesRepository,
    private val bookmarksRepository: BookmarksRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleState())
    val state: StateFlow<ArticleState> = _state.asStateFlow()

    private val _uiEvent = Channel<ArticleUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val blockedSources = preferencesRepository.blockedSources
    private val followedSources = preferencesRepository.preferredSources

    private var fetchGeneration = 0L

    init {
        observeEdition()
        observeBlockedSources()
        observeFollowedSources()
        observePreferredLayout()
    }

    private fun observeEdition() {
        combine(
            preferencesRepository.languageCode,
            preferencesRepository.countryCode,
        ) { languageCode, countryCode -> resolveEdition(languageCode, countryCode) }
            .onEach { resolved -> applyEdition(resolved) }
            .launchIn(viewModelScope)
    }

    private fun resolveEdition(languageCode: String, countryCode: String): Edition {
        val language = LanguageConstants.languages.firstOrNull { it.code == languageCode }
            ?: LanguageConstants.languages.firstOrNull { it.code == "en" }
            ?: LanguageConstants.languages.first()
        val country = countryCode.uppercase()
        val countryName = LanguageConstants.countryMap[country] ?: language.name
        return Edition(
            code = language.code,
            name = language.name,
            nativeName = countryName,
            abbreviations = listOf(country),
        )
    }

    private suspend fun applyEdition(edition: Edition) {
        val previous = _state.value.selectedCountry
        _state.update {
            it.copy(
                selectedCountry = edition,
                selectedLanguage = edition.code,
                editionName = edition.nativeName,
            )
        }
        if (previous == null || previous.abbreviations != edition.abbreviations ||
            previous.code != edition.code
        ) {
            _state.update {
                it.copy(
                    articles = emptyList(),
                    sportsArticles = emptyList(),
                    entertainmentArticles = emptyList(),
                )
            }
            resetPagination()
            fetchContent()
        }
    }

    private fun observePreferredLayout() {
        preferencesRepository.preferredLayout.onEach { layout ->
            _state.update { it.copy(preferredLayout = layout) }
        }.launchIn(viewModelScope)
    }

    private fun observeBlockedSources() {
        blockedSources.onEach { blocked ->
            _state.update { currentState ->
                currentState.copy(
                    articles = filterBlocked(currentState.articles, blocked),
                    sportsArticles = filterBlocked(currentState.sportsArticles, blocked),
                    entertainmentArticles = filterBlocked(currentState.entertainmentArticles, blocked),
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun filterBlocked(articles: List<Article>, blocked: Set<String>): List<Article> {
        return articles.filter { article ->
            val sourceName = article.source.name
            sourceName == null || sourceName !in blocked
        }
    }

    private fun observeFollowedSources() {
        followedSources.onEach { followed ->
            _state.update { currentState ->
                currentState.copy(
                    followedSources = followed,
                    articles = filterUnfollowed(currentState.articles, followed),
                    sportsArticles = filterUnfollowed(currentState.sportsArticles, followed),
                    entertainmentArticles = filterUnfollowed(currentState.entertainmentArticles, followed),
                )
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Applies the "Manage topics -> Sources" follow list to the feed. An
     * empty selection means "follow all" (no filtering). Matching is done on
     * the publisher display name reported by the API.
     */
    private fun filterUnfollowed(articles: List<Article>, followed: Set<String>): List<Article> {
        if (followed.isEmpty()) return articles
        val followedNames = NewsSources.available
            .filter { it.apiValue in followed }
            .map { it.name.lowercase() }
            .toSet()
        if (followedNames.isEmpty()) return articles
        return articles.filter { article ->
            val sourceName = article.source.name?.lowercase()
            sourceName == null || sourceName in followedNames
        }
    }

    fun onEvent(event: ArticleEvent) {
        when (event) {
            is ArticleEvent.CategorySelected -> eventReducer.onCategorySelected(event.category)
            is ArticleEvent.CategoryToggled -> eventReducer.onCategoryToggled(event.category)
            is ArticleEvent.RefreshArticles -> forceRefresh()
            is ArticleEvent.ArticleSelected -> eventReducer.onArticleSelected(event)
            is ArticleEvent.CountryLanguageChanged -> eventReducer.onCountryLanguageChanged(event)
            is ArticleEvent.ToggleBookmark -> toggleBookmark(event.article)
            is ArticleEvent.BlockSource -> blockSource(event.sourceName)
            is ArticleEvent.LoadMoreArticles -> loadMoreArticles()
            else -> {}
        }
    }

    private inner class EventReducer {
        fun onCategorySelected(rawCategory: String) {
            val category = rawCategory.lowercase()
            _state.update { it.copy(categories = setOf(category)) }
            analyticsHelper.logEvent(AnalyticsEvent.CategorySelected(category))

            if (category == "sports" && state.value.sportsArticles.isNotEmpty()) {
                _state.update { it.copy(articles = state.value.sportsArticles) }
            } else if (category == "entertainment" && state.value.entertainmentArticles.isNotEmpty()) {
                _state.update { it.copy(articles = state.value.entertainmentArticles) }
            } else {
                resetPagination()
                getNewsArticles(
                    category,
                    state.value.selectedCountry?.code?.lowercase() ?: "us",
                    state.value.selectedLanguage,
                )
            }
        }

        fun onCategoryToggled(rawCategory: String) {
            val category = rawCategory.lowercase()
            _state.update { currentState ->
                currentState.copy(categories = setOf(category))
            }
            analyticsHelper.logEvent(AnalyticsEvent.CategorySelected(category))

            resetPagination()
            getNewsArticles(
                category,
                state.value.selectedCountry?.code?.lowercase() ?: "us",
                state.value.selectedLanguage,
            )
        }

        fun onArticleSelected(event: ArticleEvent.ArticleSelected) {
            analyticsHelper.logEvent(AnalyticsEvent.ArticleClick(event.article.url))
            _state.update { it.copy(selectedArticle = event.article) }
        }

        fun onCountryLanguageChanged(event: ArticleEvent.CountryLanguageChanged) {
            // Optimistically reflect the pick in the UI, then persist it.
            // The persisted value flows back through [observeEdition],
            // which performs the reactive feed refetch for the new country.
            _state.update {
                it.copy(
                    selectedCountry = event.country,
                    selectedLanguage = event.languageCode,
                    editionName = event.country.nativeName,
                )
            }
            updateSelectedCountry(event.country, event.languageCode)
        }
    }

    private val eventReducer = EventReducer()

    private fun blockSource(sourceName: String) {
        viewModelScope.launch {
            preferencesRepository.blockSource(sourceName)
            analyticsHelper.logEvent(AnalyticsEvent.CategorySelected("blocked_$sourceName"))
        }
    }

    /** Persist the user's edition pick; the refetch happens via [observeEdition]. */
    private fun updateSelectedCountry(country: Edition, languageCode: String) {
        viewModelScope.launch {
            preferencesRepository.setEdition(
                languageCode,
                country.abbreviations.firstOrNull()?.lowercase() ?: country.code.lowercase(),
            )
        }
    }

    /**
     * Fetches the visible content for the active edition. A generation token
     * guards against a slow response from a previous edition overwriting the
     * freshly switched one.
     */
    private fun fetchContent() {
        val generation = ++fetchGeneration
        viewModelScope.launch {
            val country = state.value.selectedCountry?.abbreviations?.firstOrNull()?.lowercase()
                ?: DEFAULT_COUNTRY
            val lang = state.value.selectedLanguage

            val primaryCategory = state.value.category.ifBlank { DEFAULT_CATEGORY }
            getNewsArticles(primaryCategory, country, lang, generation).join()

            listOf("sports", "entertainment")
                .filter { it != primaryCategory.lowercase() }
                .forEach { category ->
                    delay(RATE_LIMIT_DELAY_MS)
                    if (generation != fetchGeneration) return@launch
                    getNewsArticlesCustom(category, country, lang, generation).join()
                }
        }
    }

    fun forceRefresh() {
        analyticsHelper.logEvent(AnalyticsEvent.NewsRefreshed)
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    // Keep cached articles to avoid flickering
                    error = null,
                )
            }
            resetPagination()
            fetchContent()
        }
    }

    /** Resets infinite-scroll pagination back to the first page. */
    private fun resetPagination() {
        _state.update {
            it.copy(
                isLoadingMore = false,
                currentPage = 1,
                totalArticles = null,
                canLoadMore = true,
            )
        }
    }

    /**
     * Loads the next page of the active feed and appends it to the visible
     * articles. Invoked when the user scrolls near the end of the list.
     * Guards against concurrent requests, exhausted pages, and empty feeds.
     */
    private fun loadMoreArticles() {
        val snapshot = _state.value
        if (snapshot.isLoading || snapshot.isLoadingMore ||
            !snapshot.canLoadMore || snapshot.articles.isEmpty()
        ) {
            return
        }

        val nextPage = snapshot.currentPage + 1
        val category = snapshot.category.ifBlank { DEFAULT_CATEGORY }
        val country = snapshot.selectedCountry?.abbreviations?.firstOrNull()?.lowercase()
            ?: DEFAULT_COUNTRY
        val lang = snapshot.selectedLanguage

        viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true) }
            val result = repository.getTopHeadlinesPage(
                country = country,
                category = category,
                lang = lang,
                page = nextPage,
            )
            when (result) {
                is Result.Success -> {
                    val paged = result.data
                    val blocked = blockedSources.first()
                    val followed = followedSources.first()
                    val newArticles = filterUnfollowed(
                        filterBlocked(paged.articles, blocked),
                        followed,
                    )
                    _state.update { current ->
                        val existingUrls = current.articles.map { it.url }.toSet()
                        val deduped = newArticles.filter { it.url !in existingUrls }
                        val merged = current.articles + deduped
                        val reachedEnd = paged.articles.isEmpty() ||
                            merged.size >= paged.totalArticles ||
                            deduped.isEmpty()
                        current.copy(
                            articles = merged,
                            isLoadingMore = false,
                            currentPage = nextPage,
                            totalArticles = paged.totalArticles,
                            canLoadMore = !reachedEnd,
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoadingMore = false, canLoadMore = false) }
                    result.message?.let { _uiEvent.send(ArticleUiEvent.ShowSnackbar(it)) }
                }
                is Result.GenericError -> {
                    _state.update { it.copy(isLoadingMore = false, canLoadMore = false) }
                    val message = result.error?.message
                    message?.let { _uiEvent.send(ArticleUiEvent.ShowSnackbar(it)) }
                }
                is Result.NetworkError -> {
                    _state.update { it.copy(isLoadingMore = false) }
                    _uiEvent.send(ArticleUiEvent.ShowSnackbar("No network connection"))
                }
                is Result.Loading -> _state.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun toggleBookmark(article: Article) {
        analyticsHelper.logEvent(
            AnalyticsEvent.BookmarkToggled(article.url, !article.isBookedMarked),
        )
        viewModelScope.launch {
            if (article.isBookedMarked) {
                bookmarksRepository.deleteArticle(article)
            } else {
                bookmarksRepository.insertedArticle(article)
            }
            state.value.selectedArticle?.let { selected ->
                if (selected.url == article.url) {
                    _state.update { it.copy(selectedArticle = selected.copy(isBookedMarked = !article.isBookedMarked)) }
                }
            }
        }
    }

    fun updateCategoryAndFetchArticles(categoryApiValue: String, lang: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, categories = setOf(categoryApiValue)) }
            val result = repository.getTopHeadlines(
                category = categoryApiValue,
                country = _state.value.selectedCountry?.abbreviations?.firstOrNull()?.lowercase()
                    ?: DEFAULT_COUNTRY,
                lang = lang,
            )
            when (result) {
                is Result.Success -> {
                    val blocked = blockedSources.first()
                    val followed = followedSources.first()
                    _state.update {
                        it.copy(
                            articles = filterUnfollowed(filterBlocked(result.data, blocked), followed),
                            isLoading = false,
                            error = null,
                            categories = setOf(categoryApiValue),
                        )
                    }
                    result.message?.let {
                        _uiEvent.send(ArticleUiEvent.ShowSnackbar(it))
                    }
                }
                is Result.Error -> _state.update {
                    it.copy(isLoading = false, error = result.message, articles = emptyList())
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun getNewsArticlesCustom(
        category: String,
        countryCode: String,
        lang: String = _state.value.selectedLanguage,
        generation: Long? = null,
    ): Job {
        return viewModelScope.launch {
            val normalizedCategory = category.lowercase()
            _state.update { it.copy(isLoading = true) }
            val result = repository.getTopHeadlines(
                country = countryCode,
                category = normalizedCategory,
                lang = lang,
            )
            if (generation != null && generation != fetchGeneration) return@launch
            when (result) {
                is Result.Success -> {
                    val blocked = blockedSources.first()
                    val followed = followedSources.first()
                    val filtered = filterUnfollowed(filterBlocked(result.data, blocked), followed)
                    when (normalizedCategory) {
                        "sports" -> _state.update {
                            it.copy(sportsArticles = filtered, isLoading = false, error = null)
                        }
                        "entertainment" -> _state.update {
                            it.copy(entertainmentArticles = filtered, isLoading = false, error = null)
                        }
                    }
                    result.message?.let {
                        _uiEvent.send(ArticleUiEvent.ShowSnackbar(it))
                    }
                }
                is Result.Error -> _state.update {
                    it.copy(error = result.message, isLoading = false)
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun getNewsArticles(
        category: String,
        countryCode: String,
        lang: String = state.value.selectedLanguage,
        generation: Long? = null,
    ): Job {
        return viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = repository.getTopHeadlines(
                country = countryCode,
                category = category,
                lang = lang,
            )
            if (generation != null && generation != fetchGeneration) return@launch
            when (result) {
                is Result.Success -> {
                    val blocked = blockedSources.first()
                    val followed = followedSources.first()
                    _state.update {
                        it.copy(
                            articles = filterUnfollowed(filterBlocked(result.data, blocked), followed),
                            isLoading = false,
                            error = null,
                        )
                    }
                    result.message?.let {
                        _uiEvent.send(ArticleUiEvent.ShowSnackbar(it))
                    }
                }
                is Result.Error -> _state.update {
                    val message = result.message ?: "An unexpected error occurred"
                    if (it.articles.isEmpty()) {
                        it.copy(error = message, isLoading = false)
                    } else {
                        viewModelScope.launch { _uiEvent.send(ArticleUiEvent.ShowSnackbar(message)) }
                        it.copy(isLoading = false)
                    }
                }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    companion object {
        private const val RATE_LIMIT_DELAY_MS = 1500L
        private const val DEFAULT_COUNTRY = "us"
        private const val DEFAULT_CATEGORY = "general"
    }
}
