package dev.mkao.weaver.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * GDELT TV 2.0 API — fetches TV news clips from the Internet Archive.
 * Docs: https://blog.gdeltproject.org/gdelt-2-0-television-api-debuts/
 * Rate limit: ~1 request per 5 seconds.
 */
interface GdeltApi {

    @GET("api/v2/tv/tv")
    suspend fun searchTvClips(
        @Query("query") query: String,
        @Query("mode") mode: String = "clipgallery",
        @Query("format") format: String = "json",
        @Query("maxrecords") maxRecords: Int = 50,
        @Query("sort") sort: String = "datedesc",
        @Query("timespan") timespan: String = "7days",
    ): GdeltResponse

    companion object {
        const val BASE_URL = "https://api.gdeltproject.org/"
    }
}

@JsonClass(generateAdapter = true)
data class GdeltResponse(
    @Json(name = "clips") val clips: List<GdeltClip>? = null,
)

@JsonClass(generateAdapter = true)
data class GdeltClip(
    @Json(name = "ia_show_id") val iaShowId: String,
    @Json(name = "date") val date: String,
    @Json(name = "station") val station: String,
    @Json(name = "show") val show: String,
    @Json(name = "preview_thumb") val previewThumb: String,
    @Json(name = "preview_url") val previewUrl: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "snippet") val snippet: String? = null,
)
