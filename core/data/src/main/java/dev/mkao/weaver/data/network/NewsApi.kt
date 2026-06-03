package dev.mkao.weaver.data.network

import dev.mkao.weaver.data.BuildConfig
import dev.mkao.weaver.data.network.model.NetworkNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * GNews API (https://gnews.io/).
 *
 * Endpoints:
 *  - `top-headlines`
 *  - `search`
 */
interface NewsApi {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String? = null,
        @Query("category") category: String? = null,
        @Query("lang") language: String? = null,
        @Query("max") max: Int = DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = FIRST_PAGE,
        @Query("apikey") apiKey: String = BuildConfig.GNEWS_API_KEY,
    ): NetworkNewsResponse

    @GET("search")
    suspend fun searchRequest(
        @Query("q") query: String,
        @Query("lang") language: String? = null,
        @Query("country") country: String? = null,
        @Query("max") max: Int = DEFAULT_PAGE_SIZE,
        @Query("page") page: Int = FIRST_PAGE,
        @Query("apikey") apiKey: String = BuildConfig.GNEWS_API_KEY,
    ): NetworkNewsResponse

    companion object {
        const val BASE_URL = "https://gnews.io/api/v4/"
        const val DEFAULT_PAGE_SIZE = 10
        const val FIRST_PAGE = 1
    }
}
