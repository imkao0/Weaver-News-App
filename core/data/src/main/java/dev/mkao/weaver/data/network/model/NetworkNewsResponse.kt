package dev.mkao.weaver.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Top-level GNews API envelope.
 */
@JsonClass(generateAdapter = true)
data class NetworkNewsResponse(
    @Json(name = "totalArticles") val totalArticles: Int = 0,
    @Json(name = "articles") val articles: List<NetworkArticle> = emptyList(),
)
