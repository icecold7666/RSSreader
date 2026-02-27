package com.example.rss.domain.model

/**
 * RSS源数据模型
 */
data class RssSource(
    val id: Long = 0,
    val title: String,
    val url: String,
    val description: String = "",
    val category: String = "默认分类",
    val isActive: Boolean = true,
    val lastUpdate: Long = 0,
    val imageUrl: String? = null,
    val customTitle: String? = null,
    val fetchInterval: Long = 3600000, // 1小时
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // 获取显示标题
    fun getDisplayTitle(): String = customTitle ?: title

    // 检查是否需要刷新
    fun needsRefresh(): Boolean = isActive && (lastUpdate == 0L || System.currentTimeMillis() - lastUpdate > fetchInterval)

    // 获取分类颜色
    fun getCategoryColor(): String {
        return when (category) {
            "科技" -> "#2196F3"
            "新闻" -> "#F44336"
            "博客" -> "#4CAF50"
            "娱乐" -> "#FF9800"
            "体育" -> "#9C27B0"
            "财经" -> "#607D8B"
            else -> "#795548"
        }
    }
}