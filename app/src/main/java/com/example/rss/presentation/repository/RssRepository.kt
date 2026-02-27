package com.example.rss.presentation.repository

import com.example.rss.data.service.ContentFetchService
import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.RssSourceRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class RssRepository @Inject constructor(
    private val rssSourceRepository: RssSourceRepository,
    private val contentFetchService: ContentFetchService
) {
    suspend fun ensureDefaultSources() {
        val defaults = listOf(
            RssSource(
                title = "OpenAI News",
                url = "https://openai.com/news/rss.xml",
                description = "OpenAI 官方新闻"
            ),
            RssSource(
                title = "MarkTechPost AI",
                url = "https://www.marktechpost.com/feed/",
                description = "AI 技术资讯"
            )
        )
        defaults.forEach { source ->
            if (rssSourceRepository.getSourceByUrl(source.url) == null) {
                rssSourceRepository.addSource(source)
            }
        }
    }

    suspend fun refreshSource(sourceId: Long): Result<Int> {
        val result = contentFetchService.fetchArticlesForSource(sourceId)
        if (result.isSuccess) {
            rssSourceRepository.updateLastUpdate(sourceId)
        }
        return result
    }

    suspend fun refreshAllSources(): Result<Int> {
        val sources = rssSourceRepository.getActiveSources().first()
        var total = 0
        sources.forEach { source ->
            val result = refreshSource(source.id)
            if (result.isSuccess) {
                total += result.getOrDefault(0)
            }
        }
        return Result.success(total)
    }
}
