package com.example.rss.util

import android.content.Context
import com.example.rss.domain.model.Article
import java.io.File

object OfflineArticleManager {
    private fun offlineDir(context: Context): File {
        val dir = File(context.filesDir, "offline_articles")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun getOfflineFile(context: Context, articleId: Long): File {
        return File(offlineDir(context), "article_$articleId.html")
    }

    fun isOfflineAvailable(context: Context, articleId: Long): Boolean {
        return getOfflineFile(context, articleId).exists()
    }

    fun saveOffline(context: Context, article: Article): File {
        val file = getOfflineFile(context, article.id)
        val content = if (article.content.isNotBlank()) article.content else article.description
        val wrapped = """
            <html>
              <head><meta name="viewport" content="width=device-width, initial-scale=1.0" /></head>
              <body>$content</body>
            </html>
        """.trimIndent()
        file.writeText(wrapped, Charsets.UTF_8)
        return file
    }

    fun readOffline(context: Context, articleId: Long): String? {
        val file = getOfflineFile(context, articleId)
        if (!file.exists()) return null
        return file.readText(Charsets.UTF_8)
    }
}
