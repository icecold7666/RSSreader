package com.example.rss.data.local.dao

import androidx.room.*
import com.example.rss.data.local.entity.RssSourceEntity
import kotlinx.coroutines.flow.Flow

/**
 * RSS源数据访问对象
 */
@Dao
interface RssSourceDao {

    @Query("SELECT * FROM rss_sources ORDER BY title ASC")
    fun getAllSources(): Flow<List<RssSourceEntity>>

    @Query("SELECT * FROM rss_sources WHERE isActive = 1 ORDER BY title ASC")
    fun getActiveSources(): Flow<List<RssSourceEntity>>

    @Query("SELECT * FROM rss_sources WHERE id = :id")
    suspend fun getSourceById(id: Long): RssSourceEntity?

    @Query("SELECT * FROM rss_sources WHERE url = :url LIMIT 1")
    suspend fun getSourceByUrl(url: String): RssSourceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: RssSourceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSources(sources: List<RssSourceEntity>)

    @Update
    suspend fun updateSource(source: RssSourceEntity)

    @Update
    suspend fun updateAllSources(sources: List<RssSourceEntity>)

    @Delete
    suspend fun deleteSource(source: RssSourceEntity)

    @Query("DELETE FROM rss_sources WHERE id = :id")
    suspend fun deleteSourceById(id: Long)

    @Query("UPDATE rss_sources SET isActive = 0 WHERE id = :id")
    suspend fun deactivateSource(id: Long)

    @Query("UPDATE rss_sources SET lastUpdate = :timestamp WHERE id = :id")
    suspend fun updateLastUpdate(id: Long, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM rss_sources WHERE isActive = 1")
    suspend fun getActiveSourceCount(): Int

    @Query("SELECT category FROM rss_sources WHERE isActive = 1 GROUP BY category")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM rss_sources WHERE category = :category AND isActive = 1 ORDER BY title ASC")
    fun getSourcesByCategory(category: String): Flow<List<RssSourceEntity>>
}