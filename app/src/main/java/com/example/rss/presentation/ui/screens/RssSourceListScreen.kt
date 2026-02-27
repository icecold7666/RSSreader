package com.example.rss.presentation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.rss.domain.model.RssSource
import com.example.rss.presentation.ui.components.AddRssSourceDialog
import com.example.rss.presentation.viewmodel.RssSourceViewModel
import com.example.rss.presentation.viewmodel.RssSourceUiState

/**
 * RSS源列表界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssSourceListScreen(
    viewModel: RssSourceViewModel = viewModel(),
    onAddSource: () -> Unit = {},
    onEditSource: (RssSource) -> Unit = {},
    onDeleteSource: (RssSource) -> Unit = {},
    onRefreshSource: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val rssSources by viewModel.rssSources.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS源管理") },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "添加RSS源")
                    }
                    IconButton(onClick = { viewModel.refreshAllSources() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新所有")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加RSS源")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                RssSourceUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                RssSourceUiState.Success -> {
                    if (rssSources.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.RssFeed,
                                contentDescription = "空列表",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "暂无RSS源",
                                style = MaterialTheme.typography.titleLarge
                            )
                            Text(
                                text = "点击右下角按钮添加第一个RSS源",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = rssSources,
                                key = { it.id },
                                contentType = { "rss_source" }
                            ) { source ->
                                RssSourceItem(
                                    source = source,
                                    onEdit = { onEditSource(source) },
                                    onDelete = { onDeleteSource(source) },
                                    onRefresh = { onRefreshSource(source.id) }
                                )
                            }
                        }
                    }
                }

                is RssSourceUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "错误",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = (uiState as RssSourceUiState.Error).message,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.clearError(); viewModel.loadRssSources() }) {
                            Text("重试")
                        }
                    }
                }
            }

            // 加载指示器
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // 添加RSS源对话框
    if (showAddDialog) {
        AddRssSourceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { source ->
                viewModel.addRssSource(source)
                showAddDialog = false
            }
        )
    }
}

/**
 * RSS源列表项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssSourceItem(
    source: RssSource,
    onEdit: (RssSource) -> Unit,
    onDelete: (RssSource) -> Unit,
    onRefresh: (Long) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // 标题
                    Text(
                        text = source.getDisplayTitle(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (source.isActive) Color.Unspecified else Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // URL
                    Text(
                        text = source.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 描述
                    if (source.description.isNotEmpty()) {
                        Text(
                            text = source.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 元信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            if (source.isActive) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "已激活",
                                    tint = Color.Green,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Text(
                                text = source.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = if (source.lastUpdate > 0) {
                                "上次更新: ${source.lastUpdate}"
                            } else {
                                "未更新"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 菜单按钮
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                }
            }
        }
    }

    // 下拉菜单
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = { showMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("刷新") },
            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
            onClick = {
                onRefresh(source.id)
                showMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text("编辑") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
                onEdit(source)
                showMenu = false
            }
        )
        DropdownMenuItem(
            text = { Text("删除") },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = {
                onDelete(source)
                showMenu = false
            }
        )
    }
}
