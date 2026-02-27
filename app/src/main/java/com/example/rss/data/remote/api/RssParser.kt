package com.example.rss.data.remote.api

import com.example.rss.data.local.entity.ArticleEntity
import com.example.rss.domain.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URL
import java.security.MessageDigest

/**
 * RSS解析器
 * 支持RSS 2.0、Atom 1.0和JSON Feed格式
 */
class RssParser {

    /**
     * 解析RSS 2.0格式
     */
    suspend fun parseRssFeed(content: String, sourceId: Long): Result<List<ArticleEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(StringReader(content))

                val articles = mutableListOf<ArticleEntity>()
                var inItem = false
                var title = ""
                var description = ""
                var link = ""
                var imageUrl: String? = null
                var pubDate = 0L
                var author = ""

                parser.next()
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "item" -> inItem = true
                                "title" -> if (inItem) title = parser.nextText().trim()
                                "description" -> if (inItem) description = parser.nextText().trim()
                                "link" -> if (inItem) link = parser.nextText().trim()
                                "enclosure" -> if (inItem) {
                                    val type = parser.getAttributeValue(null, "type")
                                    if (type?.startsWith("image/") == true) {
                                        imageUrl = parser.getAttributeValue(null, "url")
                                    }
                                }
                                "pubDate" -> if (inItem) {
                                    pubDate = parsePubDate(parser.nextText().trim())
                                }
                                "author" -> if (inItem) author = parser.nextText().trim()
                                "content:encoded" -> if (inItem) {
                                    description = parser.nextText().trim()
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "item" && inItem) {
                                if (title.isNotEmpty() && link.isNotEmpty()) {
                                    val article = createArticle(
                                        title = title,
                                        description = description,
                                        content = description,
                                        articleUrl = link,
                                        imageUrl = imageUrl,
                                        publishedDate = pubDate,
                                        author = author,
                                        sourceId = sourceId
                                    )
                                    articles.add(article)
                                }
                                inItem = false
                                title = ""
                                description = ""
                                link = ""
                                imageUrl = null
                                pubDate = 0L
                                author = ""
                            }
                        }
                    }
                    parser.next()
                }

                Result.success(articles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 解析Atom 1.0格式
     */
    suspend fun parseAtomFeed(content: String, sourceId: Long): Result<List<ArticleEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(StringReader(content))

                val articles = mutableListOf<ArticleEntity>()
                var inEntry = false
                var title = ""
                var contentText = ""
                var link = ""
                var imageUrl: String? = null
                var pubDate = 0L
                var author = ""

                parser.next()
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                "entry" -> inEntry = true
                                "title" -> if (inEntry) title = parser.nextText().trim()
                                "content" -> if (inEntry) contentText = parser.nextText().trim()
                                "link" -> if (inEntry) {
                                    val rel = parser.getAttributeValue(null, "rel")
                                    val href = parser.getAttributeValue(null, "href")
                                    if (rel == "alternate" || href != null) {
                                        link = href
                                    }
                                }
                                "summary" -> if (inEntry) contentText = parser.nextText().trim()
                                "published" -> if (inEntry) {
                                    pubDate = parsePubDate(parser.nextText().trim())
                                }
                                "updated" -> if (inEntry && pubDate == 0L) {
                                    pubDate = parsePubDate(parser.nextText().trim())
                                }
                                "author" -> if (inEntry) author = parser.nextText().trim()
                                "media:thumbnail" -> if (inEntry) {
                                    imageUrl = parser.getAttributeValue(null, "url")
                                }
                            }
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "entry" && inEntry) {
                                if (title.isNotEmpty() && link.isNotEmpty()) {
                                    val article = createArticle(
                                        title = title,
                                        description = contentText.take(200),
                                        content = contentText,
                                        articleUrl = link,
                                        imageUrl = imageUrl,
                                        publishedDate = pubDate,
                                        author = author,
                                        sourceId = sourceId
                                    )
                                    articles.add(article)
                                }
                                inEntry = false
                                title = ""
                                contentText = ""
                                link = ""
                                imageUrl = null
                                pubDate = 0L
                                author = ""
                            }
                        }
                    }
                    parser.next()
                }

                Result.success(articles)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 创建文章实体
     */
    private fun createArticle(
        title: String,
        description: String,
        content: String,
        articleUrl: String,
        imageUrl: String?,
        publishedDate: Long,
        author: String?,
        sourceId: Long
    ): ArticleEntity {
        return ArticleEntity(
            sourceId = sourceId,
            title = title,
            description = description,
            content = content,
            articleUrl = articleUrl,
            imageUrl = imageUrl,
            publishedDate = publishedDate,
            author = author,
            hash = generateHash(title, articleUrl)
        )
    }

    /**
     * 生成文章哈希值（用于去重）
     */
    private fun generateHash(title: String, url: String): String {
        val input = "$title|$url"
        return MessageDigest.getInstance("MD5")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    /**
     * 解析发布日期
     */
    private fun parsePubDate(dateString: String): Long {
        return try {
            // 支持多种日期格式
            when {
                dateString.contains("GMT") -> {
                    val format = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.US)
                    format.parse(dateString)?.time ?: 0L
                }
                dateString.contains("T") -> {
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                    format.parse(dateString)?.time ?: 0L
                }
                else -> {
                    val format = java.text.SimpleDateFormat("EEE, dd MMM yyyy", java.util.Locale.US)
                    format.parse(dateString)?.time ?: 0L
                }
            }
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}