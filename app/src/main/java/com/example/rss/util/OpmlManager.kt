package com.example.rss.util

import android.content.Context
import android.util.Xml
import com.example.rss.domain.model.RssSource
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import org.xmlpull.v1.XmlPullParser

object OpmlManager {
    private const val DEFAULT_FILE_NAME = "rss_sources.opml"

    fun defaultOpmlFile(context: Context): File {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        return File(dir, DEFAULT_FILE_NAME)
    }

    fun exportSources(context: Context, sources: List<RssSource>): File {
        val file = defaultOpmlFile(context)
        val outlines = sources.joinToString("\n") { source ->
            val title = escapeXml(source.getDisplayTitle())
            val url = escapeXml(source.url)
            """    <outline text="$title" title="$title" type="rss" xmlUrl="$url" />"""
        }
        val content = """
            <?xml version="1.0" encoding="UTF-8"?>
            <opml version="2.0">
              <head>
                <title>RSS Sources</title>
              </head>
              <body>
            $outlines
              </body>
            </opml>
        """.trimIndent()
        FileOutputStream(file).use { it.write(content.toByteArray(Charsets.UTF_8)) }
        return file
    }

    fun importSources(context: Context): List<RssSource> {
        val file = defaultOpmlFile(context)
        if (!file.exists()) return emptyList()

        val parser = Xml.newPullParser()
        val result = mutableListOf<RssSource>()
        FileInputStream(file).use { input ->
            parser.setInput(input, "UTF-8")
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "outline") {
                    val xmlUrl = parser.getAttributeValue(null, "xmlUrl")
                    if (!xmlUrl.isNullOrBlank()) {
                        val title = parser.getAttributeValue(null, "title")
                            ?: parser.getAttributeValue(null, "text")
                            ?: xmlUrl
                        result.add(
                            RssSource(
                                title = title,
                                url = xmlUrl
                            )
                        )
                    }
                }
                eventType = parser.next()
            }
        }
        return result
    }

    private fun escapeXml(input: String): String {
        return input
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
