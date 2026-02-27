package com.example.rss.domain.usecase

import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.ArticleRepository
import com.example.rss.domain.repository.RssSourceRepository
import com.example.rss.data.service.ContentFetchService
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * 刷新RSS源用例
 */
class RefreshRssSourcesUseCase @Inject constructor(
    private val rssSourceRepository: RssSourceRepository,
    private val articleRepository: ArticleRepository,
    private val contentFetchService: ContentFetchService? = null
) {
    /**
     * 刷新指定的RSS源
     */
    suspend operator fun invoke(sourceId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val source = rssSourceRepository.getSourceById(sourceId)
                if (source == null) {
                    Result.failure(Exception("RSS源不存在"))
                } else {
                    if (contentFetchService != null) {
                        val fetch = contentFetchService.fetchArticlesForSource(sourceId)
                        if (fetch.isSuccess) {
                            rssSourceRepository.updateLastUpdate(sourceId)
                            Result.success(Unit)
                        } else {
                            Result.failure(fetch.exceptionOrNull() ?: Exception("刷新失败"))
                        }
                    } else {
                        if (!source.needsRefresh()) {
                            Result.failure(Exception("该RSS源暂不需要刷新"))
                        } else {
                            rssSourceRepository.updateLastUpdate(sourceId)
                            Result.success(Unit)
                        }
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 刷新所有激活的RSS源
     */
    suspend fun refreshAllSources(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val sources = rssSourceRepository.getActiveSources().first()
                sources.forEach { source ->
                    if (contentFetchService == null) {
                        rssSourceRepository.updateLastUpdate(source.id)
                    } else {
                        val fetch = contentFetchService.fetchArticlesForSource(source.id)
                        if (fetch.isSuccess) {
                            rssSourceRepository.updateLastUpdate(source.id)
                        }
                    }
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
