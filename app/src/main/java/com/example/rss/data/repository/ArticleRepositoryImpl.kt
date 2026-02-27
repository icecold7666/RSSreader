package com.example.rss.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.rss.data.local.dao.ArticleDao
import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.local.entity.ArticleEntity
import com.example.rss.domain.model.Article
import com.example.rss.domain.repository.ArticleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class ArticleRepositoryImpl(
    private val articleDao: ArticleDao,
    private val rssSourceDao: RssSourceDao
) : ArticleRepository {

    private val selectedArticle = MutableStateFlow<Article?>(null)

    override fun getAllArticles(): Flow<PagingData<Article>> = pager { articleDao.getAllArticles() }

    override fun getArticlesBySource(sourceId: Long): Flow<PagingData<Article>> =
        pager { articleDao.getArticlesBySource(sourceId) }

    override fun getUnreadArticles(): Flow<PagingData<Article>> = pager { articleDao.getUnreadArticles() }

    override fun getFavoriteArticles(): Flow<PagingData<Article>> = pager { articleDao.getFavoriteArticles() }

    override fun getArticlesByCategory(category: String): Flow<PagingData<Article>> =
        pager { articleDao.getArticlesByCategory(category) }

    override fun searchArticles(query: String): Flow<PagingData<Article>> =
        pager { articleDao.searchArticles("%$query%") }

    override suspend fun getArticleById(id: Long): Article? {
        val article = articleDao.getArticleById(id)?.toDomainWithSourceTitle(rssSourceDao)
        selectedArticle.value = article
        return article
    }

    override fun getSelectedArticle(): StateFlow<Article?> = selectedArticle

    override suspend fun getArticleByUrl(url: String): Article? {
        return articleDao.getArticleByUrl(url)?.toDomainWithSourceTitle(rssSourceDao)
    }

    override suspend fun addArticle(article: Article): Long = articleDao.insertArticle(article.toEntity())

    override suspend fun addAllArticles(articles: List<Article>): List<Long> {
        return articleDao.insertAllArticles(articles.map { it.toEntity() })
    }

    override suspend fun updateArticle(article: Article) = articleDao.updateArticle(article.toEntity())

    override suspend fun deleteArticle(article: Article) = articleDao.deleteArticle(article.toEntity())

    override suspend fun deleteArticleById(id: Long) = articleDao.deleteArticleById(id)

    override suspend fun deleteArticlesBySource(sourceId: Long) = articleDao.deleteArticlesBySource(sourceId)

    override suspend fun markAsRead(articleId: Long) = articleDao.markAsRead(articleId)

    override suspend fun markAsUnread(articleId: Long) = articleDao.markAsUnread(articleId)

    override suspend fun markAsFavorite(articleId: Long) = articleDao.markAsFavorite(articleId)

    override suspend fun removeFavorite(articleId: Long) = articleDao.removeFavorite(articleId)

    override suspend fun markSourceAsRead(sourceId: Long) = articleDao.markSourceAsRead(sourceId)

    override suspend fun getUnreadCount(sourceId: Long): Int = articleDao.getUnreadCount(sourceId)

    override suspend fun getTotalUnreadCount(): Int = articleDao.getTotalUnreadCount()

    override suspend fun getFavoriteCount(): Int = articleDao.getFavoriteCount()

    override suspend fun cleanupOldArticles(cutoffDate: Long): Int = articleDao.cleanupOldArticles(cutoffDate)

    override suspend fun getArticleByHash(hash: String): Article? {
        return articleDao.getArticleByHash(hash)?.toDomainWithSourceTitle(rssSourceDao)
    }

    private fun pager(factory: () -> androidx.paging.PagingSource<Int, ArticleEntity>): Flow<PagingData<Article>> {
        return Pager(PagingConfig(pageSize = 20, enablePlaceholders = false), pagingSourceFactory = factory)
            .flow
            .map { pagingData -> pagingData.map { it.toDomain() } }
    }

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
