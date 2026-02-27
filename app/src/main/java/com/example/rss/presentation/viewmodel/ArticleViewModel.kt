package com.example.rss.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rss.domain.model.Article
import com.example.rss.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class ArticleViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val _articles = MutableStateFlow<PagingData<Article>>(PagingData.empty())
    val articles: StateFlow<PagingData<Article>> = _articles

    private val _filter = MutableStateFlow(ArticleFilter.ALL)
    val filter: StateFlow<ArticleFilter> = _filter

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var selectedSourceId: Long? = null

    init {
        loadArticles()
    }

    fun loadArticles(sourceId: Long? = selectedSourceId) {
        viewModelScope.launch {
            selectedSourceId = sourceId
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val flow = when (_filter.value) {
                    ArticleFilter.ALL -> if (sourceId == null) articleRepository.getAllArticles() else articleRepository.getArticlesBySource(sourceId)
                    ArticleFilter.UNREAD -> articleRepository.getUnreadArticles()
                    ArticleFilter.FAVORITE -> articleRepository.getFavoriteArticles()
                    ArticleFilter.CATEGORY -> articleRepository.getAllArticles()
                }
                flow.cachedIn(viewModelScope).collectLatest { _articles.value = it }
            } catch (e: Exception) {
                _errorMessage.value = "加载文章失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(articleId: Long) {
        viewModelScope.launch {
            articleRepository.markAsRead(articleId)
            loadArticles()
        }
    }

    fun markAsUnread(articleId: Long) {
        viewModelScope.launch {
            articleRepository.markAsUnread(articleId)
            loadArticles()
        }
    }

    fun toggleFavorite(articleId: Long) {
        viewModelScope.launch {
            val article = articleRepository.getArticleById(articleId)
            if (article?.isFavorite == true) {
                articleRepository.removeFavorite(articleId)
            } else {
                articleRepository.markAsFavorite(articleId)
            }
            loadArticles()
        }
    }

    fun markSourceAsRead(sourceId: Long) {
        viewModelScope.launch {
            articleRepository.markSourceAsRead(sourceId)
            loadArticles(sourceId)
        }
    }

    fun deleteArticleById(articleId: Long) {
        viewModelScope.launch {
            articleRepository.deleteArticleById(articleId)
            loadArticles(selectedSourceId)
        }
    }

    fun deleteArticlesBySource(sourceId: Long) {
        viewModelScope.launch {
            articleRepository.deleteArticlesBySource(sourceId)
            if (selectedSourceId == sourceId) {
                selectedSourceId = null
            }
            loadArticles(selectedSourceId)
        }
    }

    fun searchArticles(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                articleRepository.searchArticles(query).cachedIn(viewModelScope).collectLatest { _articles.value = it }
            } catch (e: Exception) {
                _errorMessage.value = "搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setFilter(filter: ArticleFilter) {
        _filter.value = filter
        loadArticles()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    suspend fun getUnreadCount(sourceId: Long? = null): Int {
        return if (sourceId == null) articleRepository.getTotalUnreadCount() else articleRepository.getUnreadCount(sourceId)
    }

    suspend fun getFavoriteCount(): Int = articleRepository.getFavoriteCount()

    suspend fun getArticleById(articleId: Long): Article? = articleRepository.getArticleById(articleId)
}

enum class ArticleFilter {
    ALL,
    UNREAD,
    FAVORITE,
    CATEGORY
}
