package com.example.rss.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.example.rss.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 文章数据访问对象
 */
@Dao
interface ArticleDao {

    @Query("SELECT * FROM articles ORDER BY publishedDate DESC")
    fun getAllArticles(): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE sourceId = :sourceId ORDER BY publishedDate DESC")
    fun getArticlesBySource(sourceId: Long): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE isRead = 0 ORDER BY publishedDate DESC")
    fun getUnreadArticles(): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE isFavorite = 1 ORDER BY publishedDate DESC")
    fun getFavoriteArticles(): PagingSource<Int, ArticleEntity>

    @Query(
        "SELECT a.* FROM articles a " +
            "INNER JOIN rss_sources s ON a.sourceId = s.id " +
            "WHERE s.category = :category " +
            "ORDER BY a.publishedDate DESC"
    )
    fun getArticlesByCategory(category: String): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE title LIKE :query OR description LIKE :query ORDER BY publishedDate DESC")
    fun searchArticles(query: String): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getArticleById(id: Long): ArticleEntity?

    @Query("SELECT * FROM articles WHERE articleUrl = :url LIMIT 1")
    suspend fun getArticleByUrl(url: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllArticles(articles: List<ArticleEntity>): List<Long>

    @Update
    suspend fun updateArticle(article: ArticleEntity)

    @Update
    suspend fun updateAllArticles(articles: List<ArticleEntity>)

    @Delete
    suspend fun deleteArticle(article: ArticleEntity)

    @Query("DELETE FROM articles WHERE id = :id")
    suspend fun deleteArticleById(id: Long)

    @Query("DELETE FROM articles WHERE sourceId = :sourceId")
    suspend fun deleteArticlesBySource(sourceId: Long)

    @Query("UPDATE articles SET isRead = 1 WHERE id = :articleId")
    suspend fun markAsRead(articleId: Long)

    @Query("UPDATE articles SET isRead = 0 WHERE id = :articleId")
    suspend fun markAsUnread(articleId: Long)

    @Query("UPDATE articles SET isFavorite = 1 WHERE id = :articleId")
    suspend fun markAsFavorite(articleId: Long)

    @Query("UPDATE articles SET isFavorite = 0 WHERE id = :articleId")
    suspend fun removeFavorite(articleId: Long)

    @Query("UPDATE articles SET isRead = 1 WHERE sourceId = :sourceId")
    suspend fun markSourceAsRead(sourceId: Long)

    @Query("SELECT COUNT(*) FROM articles WHERE sourceId = :sourceId AND isRead = 0")
    suspend fun getUnreadCount(sourceId: Long): Int

    @Query("SELECT COUNT(*) FROM articles WHERE isRead = 0")
    suspend fun getTotalUnreadCount(): Int

    @Query("SELECT COUNT(*) FROM articles WHERE isFavorite = 1")
    suspend fun getFavoriteCount(): Int

    @Query("DELETE FROM articles WHERE publishedDate < :cutoffDate")
    suspend fun cleanupOldArticles(cutoffDate: Long): Int

    @Query("SELECT * FROM articles WHERE hash = :hash LIMIT 1")
    suspend fun getArticleByHash(hash: String): ArticleEntity?
}
