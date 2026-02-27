package com.example.rss.data.service

import com.example.rss.data.local.dao.ArticleDao
import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.local.entity.ArticleEntity
import com.example.rss.data.remote.api.RssApiService
import com.example.rss.data.remote.api.RssParser
import com.example.rss.domain.model.Article
import com.example.rss.util.PerformanceMonitor
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.system.measureTimeMillis
import java.security.MessageDigest
import java.util.Date

/**
 * 内容获取服务
 * 负责从RSS源获取并解析文章内容
 */
class ContentFetchService @Inject constructor(
    private val rssSourceDao: RssSourceDao,
    private val articleDao: ArticleDao,
    private val rssApiService: RssApiService,
    private val rssParser: RssParser
) {

    /**
     * 从URL获取文章列表
     */
    suspend fun fetchArticlesFromUrl(url: String, sourceId: Long): Result<List<Article>> {
        return withContext(Dispatchers.IO) {
            try {
                // 获取RSS内容
                val response = rssApiService.fetchRssFeed(url)
                val content = response.string()

                // 尝试解析不同格式的RSS
                var parsedCount = 0
                var result: Result<List<ArticleEntity>>? = null
                val parseMs = measureTimeMillis {
                    result = try {
                        rssParser.parseRssFeed(content, sourceId)
                    } catch (e: Exception) {
                        try {
                            rssParser.parseAtomFeed(content, sourceId)
                        } catch (e2: Exception) {
                            return@withContext Result.failure(Exception("不支持的RSS格式"))
                        }
                    }
                }
                val parseResult = result ?: return@withContext Result.failure(Exception("解析结果为空"))
                parseResult.onSuccess { parsedCount = it.size }
                PerformanceMonitor.reportParse(sourceId, parsedCount, parseMs)

                parseResult.fold(
                    onSuccess = { articles ->
                        // 去重处理
                        val uniqueArticles = articles.filter { newArticle ->
                            val existing = articleDao.getArticleByHash(newArticle.hash)
                            existing == null
                        }
                        Result.success(uniqueArticles.map { it.toDomain() })
                    },
                    onFailure = { e ->
                        Result.failure(e)
                    }
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 生成文章哈希值
     */
    fun generateArticleHash(title: String, url: String): String {
        val input = "$title|$url"
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * 检查是否重复
     */
    suspend fun isDuplicate(content: String, sourceId: Long): Boolean {
        val hash = generateArticleHash(content, "dummy_url")
        return articleDao.getArticleByHash(hash) != null
    }

    /**
     * 批量获取源的文章
     */
    suspend fun fetchArticlesForSource(sourceId: Long): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val source = rssSourceDao.getSourceById(sourceId)
                if (source == null) {
                    Result.failure(Exception("RSS源不存在"))
                } else {
                    val result = fetchArticlesFromUrl(source.url, sourceId)
                    result.fold(
                        onSuccess = { articles ->
                            // 保存到数据库
                            if (articles.isNotEmpty()) {
                                val entities = articles.map { it.toEntity() }
                                articleDao.insertAllArticles(entities)
                            }
                            Result.success(articles.size)
                        },
                        onFailure = { e ->
                            Result.failure(e)
                        }
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 批量获取所有激活源的文章
     */
    suspend fun fetchAllSourcesArticles(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val sources = rssSourceDao.getActiveSources()
                var totalArticles = 0

                sources.collect { sourceList ->
                    sourceList.forEach { source ->
                        val result = fetchArticlesForSource(source.id)
                        if (result.isSuccess) {
                            totalArticles += result.getOrNull() ?: 0
                        }
                    }
                }

                Result.success(totalArticles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 清理旧文章
     */
    suspend fun cleanupOldArticles(days: Int = 30): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val cutoffDate = Date().time - (days * 24 * 60 * 60 * 1000L)
                val deletedCount = articleDao.cleanupOldArticles(cutoffDate)
                Result.success(deletedCount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 获取源统计信息
     */
    suspend fun getSourceStats(sourceId: Long): SourceStats {
        return withContext(Dispatchers.IO) {
            val source = rssSourceDao.getSourceById(sourceId)
            if (source == null) {
                SourceStats(0, 0, 0)
            } else {
                val totalCount = articleDao.getUnreadCount(sourceId).toLong()
                val unreadCount = articleDao.getUnreadCount(sourceId).toLong()
                val favoriteCount = articleDao.getFavoriteCount().toLong()
                SourceStats(totalCount, unreadCount, favoriteCount)
            }
        }
    }

    /**
     * 源统计信息
     */
    data class SourceStats(
        val totalCount: Long,
        val unreadCount: Long,
        val favoriteCount: Long
    )

    private fun Article.toEntity(): ArticleEntity {
        return ArticleEntity(
            id = id,
            sourceId = sourceId,
            title = title,
            description = description,
            content = content,
            articleUrl = articleUrl,
            imageUrl = imageUrl,
            publishedDate = publishedDate,
            author = author,
            isRead = isRead,
            isFavorite = isFavorite,
            hash = hash,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
