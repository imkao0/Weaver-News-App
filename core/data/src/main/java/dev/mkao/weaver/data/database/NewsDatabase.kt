package dev.mkao.weaver.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DatabaseArticle::class,
        DatabaseRecentSearch::class,
        DatabaseTableUpdate::class,
        DatabaseVideo::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class NewsDatabase : RoomDatabase() {

    abstract fun articleDao(): NewsDao

    abstract fun recentSearchDao(): RecentSearchDao

    abstract fun tableUpdateDao(): TableUpdateDao

    abstract fun videoDao(): VideoDao

    companion object {
        const val DATABASE_NAME = "NewsDB"
    }
}
