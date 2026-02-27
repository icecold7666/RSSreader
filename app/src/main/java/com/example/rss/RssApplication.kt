package com.example.rss

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.example.rss.data.local.database.DatabaseFactory
import com.example.rss.data.local.database.AppDatabase
import com.example.rss.util.PerformanceMonitor
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import android.os.SystemClock

/**
 * RSS阅读器应用入口类
 * 初始化数据库、图片加载器等组件
 */
@HiltAndroidApp
class RssApplication : Application(), ImageLoaderFactory {

    companion object {
        val processStartElapsedMs: Long = SystemClock.elapsedRealtime()
        lateinit var database: AppDatabase
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // 初始化数据库
        initDatabase()
        PerformanceMonitor.reportMemory()
    }

    /**
     * 初始化数据库
     */
    private fun initDatabase() {
        database = DatabaseFactory.getDatabase(this.applicationContext)
    }

    /**
     * 创建Coil图片加载器
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .maxSizePercent(0.02)
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .build()
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()
            }
            .logger(DebugLogger())
            .build()
    }

}
