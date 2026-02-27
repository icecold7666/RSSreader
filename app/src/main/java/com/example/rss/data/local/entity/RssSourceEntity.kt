package com.example.rss.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * RSS源实体类
 */
@Entity(tableName = "rss_sources")
data class RssSourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val description: String = "",
    val category: String = "默认分类",
    val isActive: Boolean = true,
    val lastUpdate: Long = 0,
    val imageUrl: String? = null,
    val customTitle: String? = null, // 自定义标题
    val fetchInterval: Long = 3600000 // 刷新间隔（毫秒），默认1小时
) {
    // 获取显示标题（优先使用自定义标题）
    fun getDisplayTitle(): String = customTitle ?: title

    // 检查是否需要刷新
    fun needsRefresh(currentTime: Long = System.currentTimeMillis()): Boolean {
        return isActive && (lastUpdate == 0L || currentTime - lastUpdate > fetchInterval)
    }
}