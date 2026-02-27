package com.example.rss.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.rss.data.local.dao.ArticleDao
import com.example.rss.data.local.dao.RssSourceDao
import com.example.rss.data.local.entity.ArticleEntity
import com.example.rss.data.local.entity.RssSourceEntity

/**
 * 应用数据库
 */
@Database(
    entities = [RssSourceEntity::class, ArticleEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun rssSourceDao(): RssSourceDao

    abstract fun articleDao(): ArticleDao

    companion object {
        const val DATABASE_NAME = "rss_database"
    }
}