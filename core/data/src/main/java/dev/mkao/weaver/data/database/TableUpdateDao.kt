package dev.mkao.weaver.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/** DAO for the `table_updates` freshness ledger. */
@Dao
interface TableUpdateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTableUpdateLog(tableUpdate: DatabaseTableUpdate)

    @Query("SELECT * FROM table_updates WHERE table_name = :tableName")
    suspend fun getTableLastUpdateTime(tableName: String): DatabaseTableUpdate?
}
