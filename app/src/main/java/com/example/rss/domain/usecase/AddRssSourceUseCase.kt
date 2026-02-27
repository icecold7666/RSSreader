package com.example.rss.domain.usecase

import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.RssSourceRepository
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 添加RSS源用例
 */
class AddRssSourceUseCase @Inject constructor(
    private val repository: RssSourceRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(source: RssSource): Result<Long> {
        return withContext(dispatcher) {
            try {
                val existingSource = repository.getSourceByUrl(source.url)
                if (existingSource != null) {
                    Result.failure(Exception("该RSS源已存在"))
                } else {
                    Result.success(repository.addSource(source))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
