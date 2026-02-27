package com.example.rss.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.rss.data.local.dao.RssSourceDao

/**
 * 文章实体类
 */
@Entity(
    tableName = "articles",
    foreignKeys = [ForeignKey(
        entity = RssSourceEntity::class,
        parentColumns = ["id"],
        childColumns = ["sourceId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        Index(value = ["sourceId"]),
        Index(value = ["articleUrl"], unique = true),
        Index(value = ["hash"])
    ]
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sourceId: Long,
    val title: String,
    val description: String,
    val content: String,
    val articleUrl: String,
    val imageUrl: String? = null,
    val publishedDate: Long,
    val author: String? = null,
    val isRead: Boolean = false,
    val isFavorite: Boolean = false,
    val hash: String, // 用于去重的哈希值
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 转换为领域模型（基础版本，不包含源标题）
    fun toDomain(): com.example.rss.domain.model.Article {
        return com.example.rss.domain.model.Article(
            id = id,
            sourceId = sourceId,
            sourceTitle = "未知来源",
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

    // 转换为领域模型（包含源标题）
    suspend fun toDomainWithSourceTitle(rssSourceDao: RssSourceDao): com.example.rss.domain.model.Article {
        // 获取源标题
        val source = rssSourceDao.getSourceById(sourceId)
        val sourceTitle = source?.title ?: "未知来源"

        return toDomain().copy(sourceTitle = sourceTitle)
    }

    // 获取文章简短描述（去除HTML标签，截取前N个字符）
    fun getShortDescription(maxLength: Int = 150): String {
        val cleanText = description.replace(Regex("<[^>]*>"), "").trim()
        return if (cleanText.length > maxLength) {
            cleanText.substring(0, maxLength) + "..."
        } else {
            cleanText
        }
    }

    // 获取发布时间可读格式
    fun getFormattedDate(): String {
        val now = System.currentTimeMillis()
        val diff = now - publishedDate

        return when {
            diff < 60000 -> "刚刚"
            diff < 3600000 -> "${diff / 60000}分钟前"
            diff < 86400000 -> "${diff / 3600000}小时前"
            diff < 604800000 -> "${diff / 86400000}天前"
            else -> "${diff / 604800000}周前"
        }
    }
}