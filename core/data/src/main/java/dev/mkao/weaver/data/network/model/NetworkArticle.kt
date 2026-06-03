package dev.mkao.weaver.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network DTO for a single GNews API article.
 */
@JsonClass(generateAdapter = true)
data class NetworkArticle(
    @Json(name = "title") val title: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "content") val content: String? = null,
    @Json(name = "url") val url: String,
    @Json(name = "image") val image: String? = null,
    @Json(name = "publishedAt") val publishedAt: String = "",
    @Json(name = "source") val source: NetworkSource,
)
