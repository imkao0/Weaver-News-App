package dev.mkao.weaver.features.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.mkao.weaver.data.preferences.UserPreferencesRepository
import dev.mkao.weaver.domain.analytics.AnalyticsEvent
import dev.mkao.weaver.domain.analytics.AnalyticsHelper
import dev.mkao.weaver.domain.repository.HeadlinesRepository
import dev.mkao.weaver.features.home.ArticleState
import dev.mkao.weaver.util.NewsCategories
import dev.mkao.weaver.util.NewsSources
import dev.mkao.weaver.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val repository: HeadlinesRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val analyticsHelper: AnalyticsHelper,
) : ViewModel() {

    private val _state = MutableStateFlow(ArticleState())
    val state: StateFlow<ArticleState> = _state.asStateFlow()

    init {
        preferencesRepository.preferredTopics.onEach { topics ->
            _state.update { it.copy(categories = topics.ifEmpty { setOf("general") }) }
        }.launchIn(viewModelScope)

        preferencesRepository.preferredSources.onEach { sources ->
            _state.update { it.copy(followedSources = sources) }
        }.launchIn(viewModelScope)

        fetchCategoryImages()
    }

    /**
     * Loads category thumbnails cache-first: previously fetched article
     * images are served from Room immediately, and a network refresh only
     * runs when the freshness ledger reports the thumbnail cache as stale.
     */
    private fun fetchCategoryImages() {
        viewModelScope.launch {
            val categories = NewsCategories.entries.toList()
            loadCachedCategoryImages(categories)

            if (repository.shouldRefreshCategoryThumbnails()) {
                refreshCategoryImages(categories)
                repository.markCategoryThumbnailsRefreshed()
                loadCachedCategoryImages(categories)
            }
        }
    }

    private suspend fun loadCachedCategoryImages(categories: List<NewsCategories>) =
        coroutineScope {
            val cached = categories.map { category ->
                async { category.apiValue to repository.getCachedCategoryImage(category.apiValue) }
            }.awaitAll().toMap()
            _state.update { it.copy(categoryImages = cached) }
        }

    private suspend fun refreshCategoryImages(categories: List<NewsCategories>) =
        coroutineScope {
            categories.map { category ->
                async {
                    when (
                        val result = repository.getTopHeadlines(
                            country = null,
                            category = category.apiValue,
                            lang = "en",
                        )
                    ) {
                        is Result.Success -> {
                            result.data.filter { it.image != null }.randomOrNull()?.image?.let {
                                _state.update { state ->
                                    state.copy(
                                        categoryImages = state.categoryImages +
                                            (category.apiValue to it),
                                    )
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }.awaitAll()
        }

    fun onCategoryToggled(category: String) {
        val normalizedCategory = category.lowercase()
        _state.update { currentState ->
            val newCategories = currentState.categories.toMutableSet()
            if (newCategories.contains(normalizedCategory)) {
                newCategories.remove(normalizedCategory)
            } else {
                newCategories.add(normalizedCategory)
            }
            if (newCategories.isEmpty()) {
                newCategories.add("general")
            }

            viewModelScope.launch {
                preferencesRepository.setPreferredTopics(newCategories)
            }

            currentState.copy(categories = newCategories)
        }
        analyticsHelper.logEvent(AnalyticsEvent.CategorySelected(normalizedCategory))
    }

    /**
     * Follow/unfollow a news source. Selections persist via the same DataStore
     * mechanism used for topics. An empty selection means "follow all" so the
     * feed stays unfiltered until the user makes an explicit choice.
     */
    fun onSourceToggled(sourceName: String) {
        val normalizedSource = sourceName.lowercase()
        _state.update { currentState ->
            val newSources = currentState.followedSources.toMutableSet()
            if (newSources.contains(normalizedSource)) {
                newSources.remove(normalizedSource)
            } else {
                newSources.add(normalizedSource)
            }

            viewModelScope.launch {
                preferencesRepository.setPreferredSources(newSources)
            }

            currentState.copy(followedSources = newSources)
        }
        analyticsHelper.logEvent(AnalyticsEvent.CategorySelected("source_$normalizedSource"))
    }

    /** True when the source (matched by display name) is currently followed. */
    fun isSourceFollowed(sourceName: String): Boolean =
        state.value.followedSources.contains(sourceName.lowercase())

    companion object {
        /** Display names of sources that are currently followed. */
        fun followedSourceNames(followed: Set<String>): Set<String> =
            followed.mapNotNull { key ->
                NewsSources.available.firstOrNull { it.apiValue == key }?.name?.lowercase()
                    ?: key
            }.toSet()
    }
}
