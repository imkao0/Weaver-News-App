package dev.mkao.weaver.data.database

import dev.mkao.weaver.data.network.model.NetworkArticle
import dev.mkao.weaver.domain.model.Article
import dev.mkao.weaver.domain.model.Source
import dev.mkao.weaver.domain.model.VideoItem
import java.net.URI

/**
 * Centralized three-model mappers (Network / Database / Domain). The UI never
 * touches network or database types directly.
 */

private const val FALLBACK_DESCRIPTION = "No description available."

/** Strips blank values so empty strings don't survive mapping. */
internal fun String?.orNullIfBlank(): String? = this?.takeUnless { it.isBlank() }

/**
 * Derives a human-readable source name from an article URL
 * ("https://www.bbc.com/news/…" -> "bbc.com") as a fallback if the API
 * source field is missing.
 */
internal fun String.sourceNameFromUrl(): String? =
    try {
        URI(this).host?.removePrefix("www.")
    } catch (e: Exception) {
        null
    }

fun NetworkArticle.asDatabaseModel(category: String = "general"): DatabaseArticle =
    DatabaseArticle(
        url = url,
        sourceId = null,
        sourceName = source.name ?: url.sourceNameFromUrl(),
        sourceUrl = source.url,
        author = null,
        title = title,
        content = content.orNullIfBlank(),
        description = description.orNullIfBlank() ?: FALLBACK_DESCRIPTION,
        isBookedMarked = false,
        image = image,
        publishedAt = publishedAt,
        category = category,
    )

fun NetworkArticle.asDomainModel(): Article =
    Article(
        url = url,
        source = Source(id = null, name = source.name ?: url.sourceNameFromUrl(), url = source.url),
        author = null,
        title = title,
        content = content.orNullIfBlank(),
        description = description.orNullIfBlank() ?: FALLBACK_DESCRIPTION,
        isBookedMarked = false,
        image = image,
        publishedAt = publishedAt,
    )

fun DatabaseArticle.asDomainModel(): Article =
    Article(
        url = url,
        source = Source(id = sourceId, name = sourceName, url = sourceUrl),
        author = author,
        title = title,
        content = content,
        description = description,
        isBookedMarked = isBookedMarked,
        image = image,
        publishedAt = publishedAt,
    )

fun Article.asDatabaseModel(category: String = "general"): DatabaseArticle =
    DatabaseArticle(
        url = url,
        sourceId = source.id,
        sourceName = source.name,
        sourceUrl = source.url,
        author = author,
        title = title,
        content = content,
        description = description,
        isBookedMarked = isBookedMarked,
        image = image,
        publishedAt = publishedAt,
        category = category,
    )

fun List<DatabaseArticle>.asDomainModel(): List<Article> = map { it.asDomainModel() }

fun VideoItem.asDatabaseModel(): DatabaseVideo =
    DatabaseVideo(
        videoId = videoId,
        title = title,
        channelName = channelName,
        thumbnailUrl = thumbnailUrl,
        watchUrl = watchUrl,
        publishedAt = publishedAt,
        isPlayable = isPlayable,
    )

fun DatabaseVideo.asDomainModel(): VideoItem =
    VideoItem(
        videoId = videoId,
        title = title,
        channelName = channelName,
        thumbnailUrl = thumbnailUrl,
        watchUrl = watchUrl,
        publishedAt = publishedAt,
        isPlayable = isPlayable,
    )

@JvmName("asDomainModelDatabaseVideo")
fun List<DatabaseVideo>.asDomainModel(): List<VideoItem> = map { it.asDomainModel() }
