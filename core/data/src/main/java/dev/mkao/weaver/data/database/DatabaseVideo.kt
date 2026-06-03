package dev.mkao.weaver.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a cached video entry.
 */
@Entity(tableName = "Videos")
data class DatabaseVideo(
    @PrimaryKey val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val watchUrl: String,
    val publishedAt: String,
    val isPlayable: Boolean,
)
