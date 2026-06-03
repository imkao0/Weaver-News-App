package dev.mkao.weaver.features.home

import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.Edition

data class ArticleState(
    // Data states
    val articles: List<Article> = emptyList(),
    val sportsArticles: List<Article> = emptyList(),
    val entertainmentArticles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: Set<String> = setOf("general"),
    val followedSources: Set<String> = emptySet(),
    val isLoadingMore: Boolean = false,
    val currentPage: Int = 1,
    val totalArticles: Int? = null,
    val canLoadMore: Boolean = true,

    // UI states
    val isResultsVisible: Boolean = false,
    val isSearchBarVisible: Boolean = false,
    val selectedArticle: Article? = null,
    val searchQuery: String = "",

    // Settings states
    val selectedCountry: Edition? = null,
    val selectedLanguage: String = "en",

    /** Active edition's country (e.g. "Singapore"). */
    val editionName: String? = null,
    val preferredLayout: String = "comfortable",
    val categoryImages: Map<String, String?> = emptyMap(),
) {
    val category: String get() = categories.joinToString(",")
}
