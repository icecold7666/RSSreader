package com.example.rss

import androidx.paging.PagingData
import com.example.rss.domain.model.Article
import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.ArticleRepository
import com.example.rss.domain.repository.RssSourceRepository
import com.example.rss.domain.usecase.AddRssSourceUseCase
import com.example.rss.domain.usecase.RefreshRssSourcesUseCase
import com.example.rss.presentation.viewmodel.RssSourceViewModel
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RssSourceViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadRssSources_calledMultipleTimes_observesOnlyOnce() = runTest {
        val repo = FakeRssSourceRepository()
        val vm = RssSourceViewModel(
            rssSourceRepository = repo,
            addRssSourceUseCase = AddRssSourceUseCase(repo, Dispatchers.Main),
            refreshRssSourcesUseCase = RefreshRssSourcesUseCase(repo, DummyArticleRepository())
        )

        vm.loadRssSources()
        vm.loadRssSources()
        advanceUntilIdle()

        assertEquals(1, repo.getAllSourcesCallCount.get())
    }

    @Test
    fun addRssSource_duplicate_setsErrorMessage() = runTest {
        val repo = FakeRssSourceRepository()
        val vm = RssSourceViewModel(
            rssSourceRepository = repo,
            addRssSourceUseCase = AddRssSourceUseCase(repo, Dispatchers.Main),
            refreshRssSourcesUseCase = RefreshRssSourcesUseCase(repo, DummyArticleRepository())
        )
        val existing = RssSource(title = "A", url = "https://example.com/rss")
        repo.addSource(existing)

        vm.addRssSource(existing)
        advanceUntilIdle()

        assertTrue(vm.errorMessage.value?.contains("已存在") == true)
    }
}

private class FakeRssSourceRepository : RssSourceRepository {
    private val sources = MutableStateFlow<List<RssSource>>(emptyList())
    val getAllSourcesCallCount = AtomicInteger(0)

    override fun getAllSources(): Flow<List<RssSource>> {
        getAllSourcesCallCount.incrementAndGet()
        return sources
    }

    override fun getActiveSources(): Flow<List<RssSource>> = flowOf(sources.value.filter { it.isActive })

    override suspend fun getSourceById(id: Long): RssSource? = sources.value.firstOrNull { it.id == id }

    override suspend fun getSourceByUrl(url: String): RssSource? = sources.value.firstOrNull { it.url == url }

    override suspend fun addSource(source: RssSource): Long {
        val nextId = (sources.value.maxOfOrNull { it.id } ?: 0L) + 1L
        val saved = source.copy(id = if (source.id == 0L) nextId else source.id)
        sources.value = sources.value + saved
        return saved.id
    }

    override suspend fun addAllSources(sources: List<RssSource>): List<Long> = sources.map { addSource(it) }

    override suspend fun updateSource(source: RssSource) {
        sources.value = sources.value.map { if (it.id == source.id) source else it }
    }

    override suspend fun deleteSource(source: RssSource) {
        sources.value = sources.value.filterNot { it.id == source.id }
    }

    override suspend fun deleteSourceById(id: Long) {
        sources.value = sources.value.filterNot { it.id == id }
    }

    override suspend fun deactivateSource(id: Long) {
        sources.value = sources.value.map { if (it.id == id) it.copy(isActive = false) else it }
    }

    override suspend fun updateLastUpdate(id: Long, timestamp: Long) = Unit

    override suspend fun getActiveSourceCount(): Int = sources.value.count { it.isActive }

    override fun getCategories(): Flow<List<String>> = flowOf(sources.value.map { it.category }.distinct())

    override fun getSourcesByCategory(category: String): Flow<List<RssSource>> =
        flowOf(sources.value.filter { it.category == category })
}

private class DummyArticleRepository : ArticleRepository {
    override fun getAllArticles(): Flow<PagingData<Article>> = flowOf(PagingData.empty())
    override fun getArticlesBySource(sourceId: Long): Flow<PagingData<Article>> = flowOf(PagingData.empty())
    override fun getUnreadArticles(): Flow<PagingData<Article>> = flowOf(PagingData.empty())
    override fun getFavoriteArticles(): Flow<PagingData<Article>> = flowOf(PagingData.empty())
    override fun getArticlesByCategory(category: String): Flow<PagingData<Article>> = flowOf(PagingData.empty())
    override fun searchArticles(query: String): Flow<PagingData<Article>> = flowOf(PagingData.empty())
    override suspend fun getArticleById(id: Long): Article? = null
    override fun getSelectedArticle(): StateFlow<Article?> = MutableStateFlow(null)
    override suspend fun getArticleByUrl(url: String): Article? = null
    override suspend fun addArticle(article: Article): Long = 0
    override suspend fun addAllArticles(articles: List<Article>): List<Long> = emptyList()
    override suspend fun updateArticle(article: Article) = Unit
    override suspend fun deleteArticle(article: Article) = Unit
    override suspend fun deleteArticleById(id: Long) = Unit
    override suspend fun deleteArticlesBySource(sourceId: Long) = Unit
    override suspend fun markAsRead(articleId: Long) = Unit
    override suspend fun markAsUnread(articleId: Long) = Unit
    override suspend fun markAsFavorite(articleId: Long) = Unit
    override suspend fun removeFavorite(articleId: Long) = Unit
    override suspend fun markSourceAsRead(sourceId: Long) = Unit
    override suspend fun getUnreadCount(sourceId: Long): Int = 0
    override suspend fun getTotalUnreadCount(): Int = 0
    override suspend fun getFavoriteCount(): Int = 0
    override suspend fun cleanupOldArticles(cutoffDate: Long): Int = 0
    override suspend fun getArticleByHash(hash: String): Article? = null
}
