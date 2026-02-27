package com.example.rss.data.local.database

import android.content.Context
import androidx.room.Room
import com.example.rss.data.local.entity.RssSourceEntity
import kotlinx.coroutines.runBlocking

/**
 * 数据库工厂类
 */
object DatabaseFactory {

    @Volatile
    private var instance: AppDatabase? = null
    private const val PREFS_NAME = "rss_seed_prefs"
    private const val KEY_BUILTIN_SEEDED = "builtin_seeded"
    private val defaultAiSources = listOf(
        RssSourceEntity(
            title = "OpenAI News",
            url = "https://openai.com/news/rss.xml",
            description = "OpenAI 官方新闻",
            category = "科技"
        ),
        RssSourceEntity(
            title = "MarkTechPost AI",
            url = "https://www.marktechpost.com/feed/",
            description = "AI 技术资讯",
            category = "科技"
        )
    )

    fun getDatabase(context: Context): AppDatabase {
        val db = instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }
        preloadBuiltinSourcesIfNeeded(context.applicationContext, db)
        return db
    }

    private fun preloadBuiltinSourcesIfNeeded(context: Context, database: AppDatabase) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_BUILTIN_SEEDED, false)) return

        runBlocking {
            val dao = database.rssSourceDao()
            defaultAiSources.forEach { source ->
                if (dao.getSourceByUrl(source.url) == null) {
                    dao.insertSource(source)
                }
            }
        }
        prefs.edit().putBoolean(KEY_BUILTIN_SEEDED, true).apply()
    }

    private fun buildDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
        .fallbackToDestructiveMigration()
        .build()
    }
}
