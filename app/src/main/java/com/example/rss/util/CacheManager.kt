package com.example.rss.util

import android.content.Context
import java.io.File

object CacheManager {
    fun calculateCacheSize(context: Context): Long {
        return directorySize(context.cacheDir)
    }

    fun clearAllCache(context: Context): Long {
        val before = calculateCacheSize(context)
        context.cacheDir.listFiles()?.forEach { file ->
            deleteRecursively(file)
        }
        return before
    }

    private fun directorySize(dir: File): Long {
        if (!dir.exists()) return 0L
        if (dir.isFile) return dir.length()
        return dir.listFiles()?.sumOf { directorySize(it) } ?: 0L
    }

    private fun deleteRecursively(file: File) {
        if (file.isDirectory) {
            file.listFiles()?.forEach { deleteRecursively(it) }
        }
        file.delete()
    }
}
