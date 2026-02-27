package com.example.rss.domain.usecase

import com.example.rss.domain.repository.ArticleRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 标记文章为已读用例
 */
class MarkArticleAsReadUseCase(
    private val articleRepository: ArticleRepository
) {
    suspend operator fun invoke(articleId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                articleRepository.markAsRead(articleId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 标记源下所有文章为已读
     */
    suspend fun markSourceAsRead(sourceId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                articleRepository.markSourceAsRead(sourceId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
