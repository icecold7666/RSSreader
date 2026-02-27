package com.example.rss.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rss.domain.model.RssSource
import com.example.rss.domain.repository.RssSourceRepository
import com.example.rss.domain.usecase.AddRssSourceUseCase
import com.example.rss.domain.usecase.RefreshRssSourcesUseCase
import com.example.rss.util.CacheManager
import com.example.rss.util.DatabaseBackupManager
import com.example.rss.util.OpmlManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * RSS源ViewModel
 */
@HiltViewModel
class RssSourceViewModel @Inject constructor(
    private val rssSourceRepository: RssSourceRepository,
    private val addRssSourceUseCase: AddRssSourceUseCase,
    private val refreshRssSourcesUseCase: RefreshRssSourcesUseCase
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow<RssSourceUiState>(RssSourceUiState.Loading)
    val uiState: StateFlow<RssSourceUiState> = _uiState.asStateFlow()

    // RSS源列表
    private val _rssSources = MutableStateFlow<List<RssSource>>(emptyList())
    val rssSources: StateFlow<List<RssSource>> = _rssSources.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误消息
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _operationMessage = MutableStateFlow<String?>(null)
    val operationMessage: StateFlow<String?> = _operationMessage.asStateFlow()
    private var sourcesObserveJob: Job? = null

    // 初始化
    init {
        loadRssSources()
    }

    /**
     * 加载RSS源列表
     */
    fun loadRssSources() {
        if (sourcesObserveJob?.isActive == true) return
        sourcesObserveJob = viewModelScope.launch {
            rssSourceRepository.getAllSources()
                .onStart { _isLoading.value = true }
                .catch { e ->
                    _errorMessage.value = e.message
                    _uiState.value = RssSourceUiState.Error(e.message ?: "未知错误")
                    _isLoading.value = false
                }
                .collect { sources ->
                    _rssSources.value = sources
                    _uiState.value = RssSourceUiState.Success
                    _isLoading.value = false
                }
        }
    }

    /**
     * 添加RSS源
     */
    fun addRssSource(source: RssSource) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = addRssSourceUseCase(source)
                if (result.isSuccess) {
                    _uiState.value = RssSourceUiState.Success
                } else {
                    throw result.exceptionOrNull() ?: Exception("添加失败")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _uiState.value = RssSourceUiState.Error(e.message ?: "添加失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 更新RSS源
     */
    fun updateRssSource(source: RssSource) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                rssSourceRepository.updateSource(source)
                _uiState.value = RssSourceUiState.Success
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _uiState.value = RssSourceUiState.Error(e.message ?: "更新失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 删除RSS源
     */
    fun deleteRssSource(source: RssSource) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                rssSourceRepository.deleteSource(source)
                _uiState.value = RssSourceUiState.Success
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _uiState.value = RssSourceUiState.Error(e.message ?: "删除失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 刷新指定的RSS源
     */
    fun refreshRssSource(sourceId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = refreshRssSourcesUseCase(sourceId)
                if (result.isSuccess) {
                    _uiState.value = RssSourceUiState.Success
                } else {
                    throw result.exceptionOrNull() ?: Exception("刷新失败")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _uiState.value = RssSourceUiState.Error(e.message ?: "刷新失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 刷新所有RSS源
     */
    fun refreshAllSources() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = refreshRssSourcesUseCase.refreshAllSources()
                if (result.isSuccess) {
                    _uiState.value = RssSourceUiState.Success
                } else {
                    throw result.exceptionOrNull() ?: Exception("批量刷新失败")
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _uiState.value = RssSourceUiState.Error(e.message ?: "批量刷新失败")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _errorMessage.value = null
    }

    fun clearOperationMessage() {
        _operationMessage.value = null
    }

    fun exportToOpml(context: Context) {
        viewModelScope.launch {
            try {
                val file = OpmlManager.exportSources(context, _rssSources.value)
                _operationMessage.value = "导出成功: ${file.absolutePath}"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "导出失败"
            }
        }
    }

    fun importFromOpml(context: Context) {
        viewModelScope.launch {
            try {
                val imported = OpmlManager.importSources(context)
                if (imported.isEmpty()) {
                    _operationMessage.value = "未找到可导入的OPML数据"
                    return@launch
                }
                var added = 0
                imported.forEach { source ->
                    val exists = rssSourceRepository.getSourceByUrl(source.url)
                    if (exists == null) {
                        rssSourceRepository.addSource(source)
                        added++
                    }
                }
                _operationMessage.value = "导入完成: 新增 $added / 共 ${imported.size}"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "导入失败"
            }
        }
    }

    fun backupDatabase(context: Context) {
        viewModelScope.launch {
            try {
                val file = DatabaseBackupManager.createBackup(context)
                _operationMessage.value = "备份成功: ${file.absolutePath}"
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "备份失败"
            }
        }
    }

    fun clearCache(context: Context) {
        viewModelScope.launch {
            try {
                val bytes = CacheManager.clearAllCache(context)
                val mb = bytes.toDouble() / (1024 * 1024)
                _operationMessage.value = "缓存已清理: %.2f MB".format(mb)
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "清理缓存失败"
            }
        }
    }
}

/**
 * RSS源UI状态
 */
sealed class RssSourceUiState {
    object Loading : RssSourceUiState()
    object Success : RssSourceUiState()
    data class Error(val message: String) : RssSourceUiState()
}
