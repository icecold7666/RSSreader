package com.example.rss

import androidx.paging.PagingData
import com.example.rss.domain.model.Article
import com.example.rss.domain.repository.ArticleRepository
import com.example.rss.presentation.viewmodel.ArticleFilter
import com.example.rss.presentation.viewmodel.ArticleViewModel
import java.util.concurrent.atomic.AtomicInteger
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
class ArticleViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadArticles_withSource_callsSourceQuery() = runTest {
        val repo = FakeArticleRepository()
        val vm = ArticleViewModel(repo)

        vm.loadArticles(7L)
        advanceUntilIdle()

        assertEquals(7L, repo.lastSourceIdQueried)
        assertTrue(repo.sourceQueryCount.get() > 0)
    }

    @Test
    fun setFilter_favorite_callsFavoriteQuery() = runTest {
        val repo = FakeArticleRepository()
        val vm = ArticleViewModel(repo)

        vm.setFilter(ArticleFilter.FAVORITE)
        advanceUntilIdle()

        assertTrue(repo.favoriteQueryCount.get() > 0)
    }

    @Test
    fun toggleFavorite_callsMarkOrRemove() = runTest {
        val repo = FakeArticleRepository()
        val vm = ArticleViewModel(repo)

        repo.currentArticle = repo.sampleArticle.copy(isFavorite = false)
        vm.toggleFavorite(repo.sampleArticle.id)
        advanceUntilIdle()
        assertEquals(1, repo.markFavoriteCount.get())

        repo.currentArticle = repo.sampleArticle.copy(isFavorite = true)
        vm.toggleFavorite(repo.sampleArticle.id)
        advanceUntilIdle()
        assertEquals(1, repo.removeFavoriteCount.get())
    }
}

private class FakeArticleRepository : ArticleRepository {
    val sourceQueryCount = AtomicInteger(0)
    val favoriteQueryCount = AtomicInteger(0)
    val markFavoriteCount = AtomicInteger(0)
    val removeFavoriteCount = AtomicInteger(0)
    var lastSourceIdQueried: Long? = null
    var currentArticle: Article? = null

    val sampleArticle = Article(
        id = 1L,
        sourceId = 1L,
        sourceTitle = "src",
        title = "title",
        description = "desc",
        content = "content",
        articleUrl = "https://example.com",
        publishedDate = System.currentTimeMillis(),
        hash = "h"
    )

    override fun getAllArticles(): Flow<PagingData<Article>> = flowOf(PagingData.empty())

    override fun getArticlesBySource(sourceId: Long): Flow<PagingData<Article>> {
        sourceQueryCount.incrementAndGet()
        lastSourceIdQueried = sourceId
        return flowOf(PagingData.empty())
    }

    override fun getUnreadArticles(): Flow<PagingData<Article>> = flowOf(PagingData.empty())

    override fun getFavoriteArticles(): Flow<PagingData<Article>> {
        favoriteQueryCount.incrementAndGet()
        return flowOf(PagingData.empty())
    }

    override fun getArticlesByCategory(category: String): Flow<PagingData<Article>> = flowOf(PagingData.empty())

    override fun searchArticles(query: String): Flow<PagingData<Article>> = flowOf(PagingData.empty())

    override suspend fun getArticleById(id: Long): Article? = currentArticle ?: sampleArticle

    override fun getSelectedArticle(): StateFlow<Article?> = MutableStateFlow(null)

    override suspend fun getArticleByUrl(url: String): Article? = null

    override suspend fun addArticle(article: Article): Long = 1L

    override suspend fun addAllArticles(articles: List<Article>): List<Long> = emptyList()

    override suspend fun updateArticle(article: Article) = Unit

    override suspend fun deleteArticle(article: Article) = Unit

    override suspend fun deleteArticleById(id: Long) = Unit

    override suspend fun deleteArticlesBySource(sourceId: Long) = Unit

    override suspend fun markAsRead(articleId: Long) = Unit

    override suspend fun markAsUnread(articleId: Long) = Unit

    override suspend fun markAsFavorite(articleId: Long) {
        markFavoriteCount.incrementAndGet()
    }

    override suspend fun removeFavorite(articleId: Long) {
        removeFavoriteCount.incrementAndGet()
    }

    override suspend fun markSourceAsRead(sourceId: Long) = Unit

    override suspend fun getUnreadCount(sourceId: Long): Int = 0

    override suspend fun getTotalUnreadCount(): Int = 0

    override suspend fun getFavoriteCount(): Int = 0

    override suspend fun cleanupOldArticles(cutoffDate: Long): Int = 0

    override suspend fun getArticleByHash(hash: String): Article? = null
}
