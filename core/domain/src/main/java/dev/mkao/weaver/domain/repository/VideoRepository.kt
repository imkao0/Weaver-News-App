package dev.mkao.weaver.domain.repository

import dev.mkao.weaver.domain.model.VideoItem
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    fun getVideos(perChannelLimit: Int = 6): Flow<List<VideoItem>>
    suspend fun refreshVideos(perChannelLimit: Int = 6)
}
