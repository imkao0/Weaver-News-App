package dev.mkao.weaver.features.details

import dev.mkao.weaver.domain.model.Article

data class DetailState(
    val selectedArticle: Article? = null,
    val selectedCategory: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
)
