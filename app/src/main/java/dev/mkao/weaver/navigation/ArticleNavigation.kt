package dev.mkao.weaver.navigation

import dev.mkao.weaver.domain.model.Article

/** Fallback YouTube video used when an article looks like a video but carries no id. */
private const val DEFAULT_VIDEO_ID = "dQw4w9WgXcQ"

private const val YOUTUBE_HOST = "youtube.com"
private const val YOUTUBE_SHORT_HOST = "youtu.be"
private const val VIDEO_QUERY_PARAM = "v="
private const val VIDEO_TITLE_KEYWORD = "video"

/** True when the article points to (or reads like) a video story. */
fun isVideoArticle(article: Article): Boolean =
    article.url.contains(YOUTUBE_HOST) ||
        article.url.contains(YOUTUBE_SHORT_HOST) ||
        article.title.contains(VIDEO_TITLE_KEYWORD, ignoreCase = true)

/** Extracts the YouTube video id from the article url, falling back to [DEFAULT_VIDEO_ID]. */
fun extractVideoId(article: Article): String =
    if (article.url.contains(VIDEO_QUERY_PARAM)) {
        article.url.substringAfter(VIDEO_QUERY_PARAM).substringBefore("&")
    } else {
        DEFAULT_VIDEO_ID
    }
