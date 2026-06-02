package dev.mkao.weaver.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Freshness ledger row (`table_updates` table). Records the last successful
 * sync time per logical feed key (e.g. "top_stories_general_us") so the
 * repository can throttle redundant network refreshes via
 * [TableUpdateInterval].
 */
@Entity(tableName = "table_updates")
data class DatabaseTableUpdate(
    @PrimaryKey
    val table_name: String,
    val last_updated: Long,
)

/**
 * How long a cached feed stays "fresh" before it is eligible for a network
 * refresh.
 */
enum class TableUpdateInterval(val intervalMillis: Long) {
    TOP_STORIES(30L * 60L * 1000L),
    BACKGROUND_REFRESH(6L * 60L * 60L * 1000L),
    VIDEOS(2L * 60L * 60L * 1000L),
    CATEGORY_THUMBNAILS(6L * 60L * 60L * 1000L),
}
