package com.example.rss.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.rss.domain.model.Article
import com.example.rss.domain.repository.ArticleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 搜索视图模型
 */
@HiltViewModel
@OptIn(FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val articleRepository: ArticleRepository
) : ViewModel() {
    private var searchJob: Job? = null

    // 查询状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    // 搜索结果
    private val _searchResults = MutableStateFlow<PagingData<Article>>(PagingData.empty())
    val searchResults: StateFlow<PagingData<Article>> = _searchResults

    // 搜索历史
    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 初始化
    init {
        // 自动保存搜索历史
        setupAutoSearch()
    }

    /**
     * 设置自动搜索（防抖动）
     */
    private fun setupAutoSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500L) // 防抖500ms
                .distinctUntilChanged()
                .collect { query ->
                    if (query.isNotBlank()) {
                        performSearch(query)
                    }
                }
        }
    }

    /**
     * 执行搜索
     */
    fun search(query: String) {
        _searchQuery.value = query
    }

    /**
     * 执行实际的搜索操作
     */
    fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                articleRepository.searchArticles(query)
                    .cachedIn(viewModelScope)
                    .collectLatest { pagingData ->
                        _searchResults.value = pagingData
                    }

                // 添加到搜索历史
                addToHistory(query)
            } catch (e: Exception) {
                _errorMessage.value = "搜索失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除搜索
     */
    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = PagingData.empty()
    }

    /**
     * 从搜索历史中删除项
     */
    fun removeFromHistory(query: String) {
        _searchHistory.value = _searchHistory.value - query
    }

    /**
     * 清除所有搜索历史
     */
    fun clearHistory() {
        _searchHistory.value = emptyList()
    }

    /**
     * 添加到搜索历史
     */
    private fun addToHistory(query: String) {
        val currentHistory = _searchHistory.value.toMutableList()

        // 避免重复添加
        if (!currentHistory.contains(query)) {
            currentHistory.add(0, query) // 添加到开头

            // 限制历史记录数量
            if (currentHistory.size > 20) {
                currentHistory.removeLast()
            }

            _searchHistory.value = currentHistory
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    fun markAsRead(articleId: Long) {
        viewModelScope.launch {
            articleRepository.markAsRead(articleId)
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
        }
    }

    /**
     * 获取热门搜索建议（可以根据实际需求实现）
     */
    fun getSuggestions(query: String): List<String> {
        return _searchHistory.value
            .filter { it.contains(query, ignoreCase = true) }
            .take(5)
    }
}
