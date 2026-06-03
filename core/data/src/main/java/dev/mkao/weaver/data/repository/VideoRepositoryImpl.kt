package dev.mkao.weaver.data.repository

import dev.mkao.weaver.data.database.DatabaseTableUpdate
import dev.mkao.weaver.data.database.TableUpdateDao
import dev.mkao.weaver.data.database.TableUpdateInterval
import dev.mkao.weaver.data.database.VideoDao
import dev.mkao.weaver.data.database.asDatabaseModel
import dev.mkao.weaver.data.database.asDomainModel
import dev.mkao.weaver.data.network.GdeltApi
import dev.mkao.weaver.data.network.GdeltClip
import dev.mkao.weaver.data.network.VideoChannels
import dev.mkao.weaver.data.network.YouTubeFeedParser
import dev.mkao.weaver.data.network.YouTubeRssApi
import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.domain.repository.VideoRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Fetches and merges the latest videos from free YouTube channel RSS feeds
 * and the GDELT TV 2.0 API (Internet Archive TV News clips).
 *
 * Channels are fetched in parallel; individual failures are ignored.
 */
@Singleton
class VideoRepositoryImpl @Inject constructor(
    private val api: YouTubeRssApi,
    private val gdeltApi: GdeltApi,
    private val videoDao: VideoDao,
    private val tableUpdateDao: TableUpdateDao,
) : VideoRepository {

    override fun getVideos(perChannelLimit: Int): Flow<List<VideoItem>> =
        videoDao.getVideos().map { it.asDomainModel() }

    override suspend fun refreshVideos(perChannelLimit: Int) {
        if (isFresh("videos", TableUpdateInterval.VIDEOS.intervalMillis)) {
            Timber.tag("VideoRepo").d("Video cache is fresh; skipping refresh")
            return
        }

        try {
            val videos = fetchLatestVideos(perChannelLimit)
            if (videos.isNotEmpty()) {
                videoDao.clearAll()
                videoDao.upsertAll(videos.map { it.asDatabaseModel() })
                recordFreshness("videos")
            }
        } catch (e: Exception) {
            Timber.tag("VideoRepo").e(e, "Failed to refresh videos")
        }
    }

    private suspend fun fetchLatestVideos(perChannelLimit: Int): List<VideoItem> =
        coroutineScope {
            val youtubeJobs = VideoChannels.feeds.map { channel ->
                async(Dispatchers.IO) {
                    runCatching {
                        val xml = api.getChannelFeed(channel.channelId).string()
                        withContext(Dispatchers.Default) {
                            YouTubeFeedParser.parse(xml, channel.name)
                        }
                    }.getOrDefault(emptyList()).take(perChannelLimit)
                }
            }

            val gdeltJob = async(Dispatchers.IO) {
                runCatching {
                    gdeltApi.searchTvClips(query = "station:CNN", maxRecords = 20).clips
                        ?.map { it.toVideoItem() }
                        .orEmpty()
                }.getOrDefault(emptyList())
            }

            val allVideos = (youtubeJobs.awaitAll().flatten() + gdeltJob.await())

            allVideos
                .distinctBy { it.videoId }
                .sortedByDescending { it.publishedAt }
        }

    private suspend fun isFresh(key: String, intervalMillis: Long): Boolean {
        val last = tableUpdateDao.getTableLastUpdateTime(key)?.last_updated ?: return false
        return System.currentTimeMillis() - last < intervalMillis
    }

    private suspend fun recordFreshness(key: String) {
        tableUpdateDao.insertTableUpdateLog(
            DatabaseTableUpdate(
                table_name = key,
                last_updated = System.currentTimeMillis(),
            ),
        )
    }

    private fun GdeltClip.toVideoItem(): VideoItem = VideoItem(
        videoId = iaShowId,
        title = title ?: show,
        channelName = station,
        thumbnailUrl = previewThumb,
        watchUrl = "https://archive.org/download/$iaShowId/$iaShowId.mp4",
        publishedAt = date,
        isPlayable = true,
    )
}
