package com.example.rss

import androidx.paging.PagingData
import com.example.rss.domain.model.Article
import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.ArticleRepository
import com.example.rss.domain.repository.RssSourceRepository
import com.example.rss.domain.usecase.AddRssSourceUseCase
import com.example.rss.domain.usecase.MarkArticleAsReadUseCase
import com.example.rss.domain.usecase.RefreshRssSourcesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UseCaseTest {

    @Test
    fun addRssSourceUseCase_rejectsDuplicateUrl() = runTest {
        val repo = FakeRssRepo(existingUrl = "https://dup.com/rss")
        val useCase = AddRssSourceUseCase(repo)
        val result = useCase(RssSource(title = "dup", url = "https://dup.com/rss"))
        assertTrue(result.isFailure)
    }

    @Test
    fun addRssSourceUseCase_addsWhenNotExists() = runTest {
        val repo = FakeRssRepo()
        val useCase = AddRssSourceUseCase(repo)
        val result = useCase(RssSource(title = "new", url = "https://new.com/rss"))
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun markArticleAsReadUseCase_marksSingleAndSource() = runTest {
        val articleRepo = FakeArticleRepo()
        val useCase = MarkArticleAsReadUseCase(articleRepo)

        val single = useCase(10L)
        val source = useCase.markSourceAsRead(2L)

        assertTrue(single.isSuccess)
        assertTrue(source.isSuccess)
        assertEquals(10L, articleRepo.lastReadId)
        assertEquals(2L, articleRepo.lastSourceReadId)
    }

    @Test
    fun refreshRssSourcesUseCase_returnsFailure_whenSourceMissing() = runTest {
        val rssRepo = FakeRssRepo()
        val articleRepo = FakeArticleRepo()
        val useCase = RefreshRssSourcesUseCase(rssRepo, articleRepo)
        val result = useCase(99L)
        assertTrue(result.isFailure)
    }

    @Test
    fun refreshRssSourcesUseCase_updatesLastUpdate_whenRefreshNeeded() = runTest {
        val source = RssSource(
            id = 3L,
            title = "need refresh",
            url = "https://ok.com/rss",
            isActive = true,
            lastUpdate = 0L
        )
        val rssRepo = FakeRssRepo(sourceById = source)
        val articleRepo = FakeArticleRepo()
        val useCase = RefreshRssSourcesUseCase(rssRepo, articleRepo)

        val result = useCase(3L)
        assertTrue(result.isSuccess)
        assertEquals(3L, rssRepo.lastUpdatedSourceId)
    }

    @Test
    fun refreshRssSourcesUseCase_returnsFailure_whenNoNeedRefresh() = runTest {
        val source = RssSource(
            id = 4L,
            title = "fresh",
            url = "https://fresh.com/rss",
            isActive = true,
            lastUpdate = System.currentTimeMillis()
        )
        val rssRepo = FakeRssRepo(sourceById = source)
        val articleRepo = FakeArticleRepo()
        val useCase = RefreshRssSourcesUseCase(rssRepo, articleRepo)

        val result = useCase(4L)
        assertFalse(result.isSuccess)
    }
}

private class FakeRssRepo(
    private val existingUrl: String? = null,
    private val sourceById: RssSource? = null
) : RssSourceRepository {
    var lastUpdatedSourceId: Long? = null

    override fun getAllSources(): Flow<List<RssSource>> = flowOf(emptyList())
    override fun getActiveSources(): Flow<List<RssSource>> = flowOf(emptyList())
    override suspend fun getSourceById(id: Long): RssSource? = sourceById?.takeIf { it.id == id }
    override suspend fun getSourceByUrl(url: String): RssSource? =
        if (url == existingUrl) RssSource(title = "exists", url = existingUrl) else null
    override suspend fun addSource(source: RssSource): Long = 1L
    override suspend fun addAllSources(sources: List<RssSource>): List<Long> = sources.indices.map { (it + 1).toLong() }
    override suspend fun updateSource(source: RssSource) = Unit
    override suspend fun deleteSource(source: RssSource) = Unit
    override suspend fun deleteSourceById(id: Long) = Unit
    override suspend fun deactivateSource(id: Long) = Unit
    override suspend fun updateLastUpdate(id: Long, timestamp: Long) {
        lastUpdatedSourceId = id
    }
    override suspend fun getActiveSourceCount(): Int = 0
    override fun getCategories(): Flow<List<String>> = flowOf(emptyList())
    override fun getSourcesByCategory(category: String): Flow<List<RssSource>> = flowOf(emptyList())
}

private class FakeArticleRepo : ArticleRepository {
    var lastReadId: Long? = null
    var lastSourceReadId: Long? = null

    override fun getAllArticles(): Flow<PagingData<Article>> = emptyFlow()
    override fun getArticlesBySource(sourceId: Long): Flow<PagingData<Article>> = emptyFlow()
    override fun getUnreadArticles(): Flow<PagingData<Article>> = emptyFlow()
    override fun getFavoriteArticles(): Flow<PagingData<Article>> = emptyFlow()
    override fun getArticlesByCategory(category: String): Flow<PagingData<Article>> = emptyFlow()
    override fun searchArticles(query: String): Flow<PagingData<Article>> = emptyFlow()
    override suspend fun getArticleById(id: Long): Article? = null
    override fun getSelectedArticle(): StateFlow<Article?> = MutableStateFlow(null)
    override suspend fun getArticleByUrl(url: String): Article? = null
    override suspend fun addArticle(article: Article): Long = 0
    override suspend fun addAllArticles(articles: List<Article>): List<Long> = emptyList()
    override suspend fun updateArticle(article: Article) = Unit
    override suspend fun deleteArticle(article: Article) = Unit
    override suspend fun deleteArticleById(id: Long) = Unit
    override suspend fun deleteArticlesBySource(sourceId: Long) = Unit
    override suspend fun markAsRead(articleId: Long) { lastReadId = articleId }
    override suspend fun markAsUnread(articleId: Long) = Unit
    override suspend fun markAsFavorite(articleId: Long) = Unit
    override suspend fun removeFavorite(articleId: Long) = Unit
    override suspend fun markSourceAsRead(sourceId: Long) { lastSourceReadId = sourceId }
    override suspend fun getUnreadCount(sourceId: Long): Int = 0
    override suspend fun getTotalUnreadCount(): Int = 0
    override suspend fun getFavoriteCount(): Int = 0
    override suspend fun cleanupOldArticles(cutoffDate: Long): Int = 0
    override suspend fun getArticleByHash(hash: String): Article? = null
}
