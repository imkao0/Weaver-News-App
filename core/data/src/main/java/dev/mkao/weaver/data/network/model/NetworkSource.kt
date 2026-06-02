package dev.mkao.weaver.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Network DTO for a news source.
 */
@JsonClass(generateAdapter = true)
data class NetworkSource(
    @Json(name = "name") val name: String?,
    @Json(name = "url") val url: String?,
)
