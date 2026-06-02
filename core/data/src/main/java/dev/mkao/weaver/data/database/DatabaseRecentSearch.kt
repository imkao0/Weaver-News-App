package dev.mkao.weaver.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.mkao.weaver.domain.model.RecentSearch

/** Persisted recent-search row (`recent_searches` table). */
@Entity(tableName = "recent_searches")
data class DatabaseRecentSearch(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val query: String,
    val searchTime: Long,
)

fun List<DatabaseRecentSearch>.asDomainModel(): List<RecentSearch> = map {
    RecentSearch(
        id = it.id,
        query = it.query,
        searchTime = it.searchTime,
    )
}
