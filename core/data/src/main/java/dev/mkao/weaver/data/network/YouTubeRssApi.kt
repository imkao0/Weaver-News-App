package dev.mkao.weaver.data.network

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the free, keyless YouTube channel RSS (Atom) feeds.
 * Returns raw XML which is parsed by [YouTubeFeedParser].
 */
interface YouTubeRssApi {

    @GET("feeds/videos.xml")
    suspend fun getChannelFeed(
        @Query("channel_id") channelId: String,
    ): ResponseBody

    companion object {
        const val BASE_URL = "https://www.youtube.com/"
    }
}
