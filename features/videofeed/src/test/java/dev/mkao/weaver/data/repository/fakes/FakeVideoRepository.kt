package dev.mkao.weaver.data.repository.fakes

import dev.mkao.weaver.domain.model.VideoItem
import dev.mkao.weaver.domain.repository.VideoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

class FakeVideoRepository : VideoRepository {
    private val videosFlow = MutableStateFlow<List<VideoItem>>(emptyList())
    var shouldFailRefresh = false

    fun setVideos(videos: List<VideoItem>) {
        videosFlow.value = videos
    }

    override fun getVideos(perChannelLimit: Int): Flow<List<VideoItem>> = videosFlow

    override suspend fun refreshVideos(perChannelLimit: Int) {
        if (shouldFailRefresh) {
            throw IOException("Refresh failed")
        }
    }
}
