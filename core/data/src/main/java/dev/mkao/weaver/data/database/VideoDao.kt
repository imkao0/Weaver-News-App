package dev.mkao.weaver.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(videos: List<DatabaseVideo>)

    @Query("SELECT * FROM Videos ORDER BY publishedAt DESC")
    fun getVideos(): Flow<List<DatabaseVideo>>

    @Query("DELETE FROM Videos")
    suspend fun clearAll()
}
