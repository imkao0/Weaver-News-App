package dev.mkao.weaver.domain.model

/**
 * Pure domain model for a news article, decoupled from the Room entity
 * ([dev.mkao.weaver.data.database.DatabaseArticle]) and the network DTO
 * ([dev.mkao.weaver.data.network.model.NetworkArticle]).
 */
data class Article(
    val url: String,
    val source: Source,
    val author: String?,
    val title: String,
    val content: String?,
    val description: String?,
    val isBookedMarked: Boolean = false,
    val image: String?,
    val publishedAt: String,
)
