package com.example.rss.domain.model

/**
 * 文章数据模型
 */
data class Article(
    val id: Long = 0,
    val sourceId: Long,
    val sourceTitle: String,
    val title: String,
    val description: String,
    val content: String,
    val articleUrl: String,
    val imageUrl: String? = null,
    val publishedDate: Long,
    val author: String? = null,
    val isRead: Boolean = false,
    val isFavorite: Boolean = false,
    val hash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 获取文章简短描述
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

    // 获取状态文本
    fun getStatusText(): String {
        return when {
            isFavorite -> "已收藏"
            isRead -> "已读"
            else -> "未读"
        }
    }

    // 获取状态颜色
    fun getStatusColor(): String {
        return when {
            isFavorite -> "#FFC107" // 黄色
            isRead -> "#4CAF50" // 绿色
            else -> "#2196F3" // 蓝色
        }
    }
}