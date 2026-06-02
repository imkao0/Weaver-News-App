package dev.mkao.weaver.domain.model

/**
 * Pure domain model for a video entry in the video feed.
 *
 * Backed by free, keyless YouTube channel RSS (Atom) feeds — playback happens
 * by handing [watchUrl] to the YouTube app / browser
 */
data class VideoItem(
    val videoId: String,
    val title: String,
    val channelName: String,
    val thumbnailUrl: String,
    val watchUrl: String,
    val publishedAt: String,
    val isPlayable: Boolean = false,
)
