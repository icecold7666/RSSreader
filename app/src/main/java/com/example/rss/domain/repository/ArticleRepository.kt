package com.example.rss.domain.repository

import androidx.paging.PagingData
import com.example.rss.domain.model.Article
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * 文章仓库接口
 */
interface ArticleRepository {
    /**
     * 获取所有文章（分页）
     */
    fun getAllArticles(): Flow<PagingData<Article>>

    /**
     * 根据RSS源ID获取文章
     */
    fun getArticlesBySource(sourceId: Long): Flow<PagingData<Article>>

    /**
     * 获取未读文章
     */
    fun getUnreadArticles(): Flow<PagingData<Article>>

    /**
     * 获取收藏文章
     */
    fun getFavoriteArticles(): Flow<PagingData<Article>>

    /**
     * 根据分类获取文章
     */
    fun getArticlesByCategory(category: String): Flow<PagingData<Article>>

    /**
     * 搜索文章
     */
    fun searchArticles(query: String): Flow<PagingData<Article>>

    /**
     * 根据ID获取文章
     */
    suspend fun getArticleById(id: Long): Article?

    /**
     * 获取当前选中的文章流
     */
    fun getSelectedArticle(): StateFlow<Article?>

    /**
     * 根据URL获取文章
     */
    suspend fun getArticleByUrl(url: String): Article?

    /**
     * 添加文章
     */
    suspend fun addArticle(article: Article): Long

    /**
     * 批量添加文章
     */
    suspend fun addAllArticles(articles: List<Article>): List<Long>

    /**
     * 更新文章
     */
    suspend fun updateArticle(article: Article)

    /**
     * 删除文章
     */
    suspend fun deleteArticle(article: Article)

    /**
     * 根据ID删除文章
     */
    suspend fun deleteArticleById(id: Long)

    /**
     * 根据RSS源ID删除所有文章
     */
    suspend fun deleteArticlesBySource(sourceId: Long)

    /**
     * 标记为已读
     */
    suspend fun markAsRead(articleId: Long)

    /**
     * 标记为未读
     */
    suspend fun markAsUnread(articleId: Long)

    /**
     * 标记为收藏
     */
    suspend fun markAsFavorite(articleId: Long)

    /**
     * 取消收藏
     */
    suspend fun removeFavorite(articleId: Long)

    /**
     * 标记源下所有文章为已读
     */
    suspend fun markSourceAsRead(sourceId: Long)

    /**
     * 获取未读数量
     */
    suspend fun getUnreadCount(sourceId: Long): Int

    /**
     * 获取总未读数量
     */
    suspend fun getTotalUnreadCount(): Int

    /**
     * 获取收藏数量
     */
    suspend fun getFavoriteCount(): Int

    /**
     * 清理旧文章
     */
    suspend fun cleanupOldArticles(cutoffDate: Long): Int

    /**
     * 根据哈希值检查文章是否存在
     */
    suspend fun getArticleByHash(hash: String): Article?
}