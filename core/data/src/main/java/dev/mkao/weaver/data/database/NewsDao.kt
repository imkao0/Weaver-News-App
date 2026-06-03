package dev.mkao.weaver.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: DatabaseArticle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(articles: List<DatabaseArticle>)

    @Query("SELECT * FROM Articles ORDER BY publishedAt DESC")
    fun getArticles(): Flow<List<DatabaseArticle>>

    @Query("SELECT * FROM Articles WHERE LOWER(category) = LOWER(:category) ORDER BY publishedAt DESC")
    fun getArticlesByCategory(category: String): Flow<List<DatabaseArticle>>

    @Query(
        "SELECT image FROM Articles WHERE LOWER(category) = LOWER(:category) " +
            "AND image IS NOT NULL ORDER BY publishedAt DESC LIMIT 1",
    )
    suspend fun getLatestImageForCategory(category: String): String?

    @Query("SELECT * FROM Articles WHERE isBookedMarked = 1 ORDER BY publishedAt DESC")
    fun getBookedArticles(): Flow<List<DatabaseArticle>>

    @Query("SELECT * FROM Articles WHERE url = :url LIMIT 1")
    fun getArticleByUrl(url: String): Flow<DatabaseArticle?>

    @Query("SELECT EXISTS(SELECT 1 FROM Articles WHERE url = :url AND isBookedMarked = 1)")
    fun isBookmarked(url: String): Flow<Boolean>

    @Query("UPDATE Articles SET isBookedMarked = :isBookedMarked WHERE url = :url")
    suspend fun setBookmark(url: String, isBookedMarked: Boolean)

    @Query("UPDATE Articles SET isBookedMarked = 0 WHERE isBookedMarked = 1")
    suspend fun clearBookmarks()

    @Query("SELECT url FROM Articles WHERE isBookedMarked = 1")
    suspend fun getBookmarkedUrls(): List<String>
}
