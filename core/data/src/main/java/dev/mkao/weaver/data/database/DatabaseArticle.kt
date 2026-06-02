package dev.mkao.weaver.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for a cached news article (the local SSOT table).
 *
 * `Source` is flattened into `source_*` columns so the legacy Gson-based
 * `NewsTypeConvertor` can be removed entirely.
 */
@Entity(tableName = "Articles")
data class DatabaseArticle(
    @PrimaryKey val url: String,
    val sourceId: String?,
    val sourceName: String?,
    val sourceUrl: String?,
    val author: String?,
    val title: String,
    val content: String?,
    val description: String?,
    val isBookedMarked: Boolean = false,
    val image: String?,
    val publishedAt: String,
    val category: String = "general",
)
