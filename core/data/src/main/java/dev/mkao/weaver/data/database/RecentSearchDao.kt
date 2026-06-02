package dev.mkao.weaver.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** DAO for the `recent_searches` table; reads are reactive. */
@Dao
interface RecentSearchDao {

    @Query("SELECT * FROM recent_searches ORDER BY searchTime DESC LIMIT :maxRows")
    fun getRecentSearches(maxRows: Int = MAX_RECENT_SEARCHES): Flow<List<DatabaseRecentSearch>>

    @Query("SELECT * FROM recent_searches WHERE query = :query LIMIT 1")
    suspend fun getRecentSearchByQuery(query: String): DatabaseRecentSearch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(recentSearch: DatabaseRecentSearch)

    @Query("DELETE FROM recent_searches WHERE query = :query")
    suspend fun deleteByQuery(query: String)

    @Query("DELETE FROM recent_searches")
    suspend fun clearAll()

    companion object {
        const val MAX_RECENT_SEARCHES = 15
    }
}
