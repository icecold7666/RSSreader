package com.example.rss.domain.repository

import com.example.rss.domain.model.RssSource
import kotlinx.coroutines.flow.Flow

/**
 * RSS源仓库接口
 */
interface RssSourceRepository {
    /**
     * 获取所有RSS源
     */
    fun getAllSources(): Flow<List<RssSource>>

    /**
     * 获取激活的RSS源
     */
    fun getActiveSources(): Flow<List<RssSource>>

    /**
     * 根据ID获取RSS源
     */
    suspend fun getSourceById(id: Long): RssSource?

    /**
     * 根据URL获取RSS源
     */
    suspend fun getSourceByUrl(url: String): RssSource?

    /**
     * 添加RSS源
     */
    suspend fun addSource(source: RssSource): Long

    /**
     * 批量添加RSS源
     */
    suspend fun addAllSources(sources: List<RssSource>): List<Long>

    /**
     * 更新RSS源
     */
    suspend fun updateSource(source: RssSource)

    /**
     * 删除RSS源
     */
    suspend fun deleteSource(source: RssSource)

    /**
     * 根据ID删除RSS源
     */
    suspend fun deleteSourceById(id: Long)

    /**
     * 停用RSS源
     */
    suspend fun deactivateSource(id: Long)

    /**
     * 更新最后刷新时间
     */
    suspend fun updateLastUpdate(id: Long, timestamp: Long = System.currentTimeMillis())

    /**
     * 获取激活源数量
     */
    suspend fun getActiveSourceCount(): Int

    /**
     * 获取所有分类
     */
    fun getCategories(): Flow<List<String>>

    /**
     * 根据分类获取RSS源
     */
    fun getSourcesByCategory(category: String): Flow<List<RssSource>>
}