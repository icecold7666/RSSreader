package com.example.rss.data.repository

import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.local.entity.RssSourceEntity
import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.RssSourceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * RSS源仓库实现
 */
class RssSourceRepositoryImpl(
    private val rssSourceDao: RssSourceDao
) : RssSourceRepository {

    override fun getAllSources(): Flow<List<RssSource>> {
        return rssSourceDao.getAllSources().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override fun getActiveSources(): Flow<List<RssSource>> {
        return rssSourceDao.getActiveSources().map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    override suspend fun getSourceById(id: Long): RssSource? {
        return rssSourceDao.getSourceById(id)?.toDomain()
    }

    override suspend fun getSourceByUrl(url: String): RssSource? {
        return rssSourceDao.getSourceByUrl(url)?.toDomain()
    }

    override suspend fun addSource(source: RssSource): Long {
        val entity = source.toEntity()
        return rssSourceDao.insertSource(entity)
    }

    override suspend fun addAllSources(sources: List<RssSource>): List<Long> {
        return sources.map { source ->
            rssSourceDao.insertSource(source.toEntity())
        }
    }

    override suspend fun updateSource(source: RssSource) {
        val entity = source.toEntity()
        rssSourceDao.updateSource(entity)
    }

    override suspend fun deleteSource(source: RssSource) {
        val entity = source.toEntity()
        rssSourceDao.deleteSource(entity)
    }

    override suspend fun deleteSourceById(id: Long) {
        rssSourceDao.deleteSourceById(id)
    }

    override suspend fun deactivateSource(id: Long) {
        rssSourceDao.deactivateSource(id)
    }

    override suspend fun updateLastUpdate(id: Long, timestamp: Long) {
        rssSourceDao.updateLastUpdate(id, timestamp)
    }

    override suspend fun getActiveSourceCount(): Int {
        return rssSourceDao.getActiveSourceCount()
    }

    override fun getCategories(): Flow<List<String>> {
        return rssSourceDao.getCategories()
    }

    override fun getSourcesByCategory(category: String): Flow<List<RssSource>> {
        return rssSourceDao.getSourcesByCategory(category).map { entities ->
            entities.map { entity -> entity.toDomain() }
        }
    }

    /**
     * 转换为领域模型
     */
    private fun RssSourceEntity.toDomain(): RssSource {
        return RssSource(
            id = id,
            title = title,
            url = url,
            description = description,
            category = category,
            isActive = isActive,
            lastUpdate = lastUpdate,
            imageUrl = imageUrl,
            customTitle = customTitle,
            fetchInterval = fetchInterval
        )
    }

    /**
     * 转换为实体
     */
    private fun RssSource.toEntity(): RssSourceEntity {
        return RssSourceEntity(
            id = id,
            title = title,
            url = url,
            description = description,
            category = category,
            isActive = isActive,
            lastUpdate = lastUpdate,
            imageUrl = imageUrl,
            customTitle = customTitle,
            fetchInterval = fetchInterval
        )
    }
}
