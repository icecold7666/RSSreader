package com.example.rss.presentation.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.rss.domain.model.Article
import com.example.rss.domain.model.RssSource
import com.example.rss.presentation.navigation.ADD_RSS_SOURCE_ROUTE
import com.example.rss.presentation.navigation.ARTICLE_DETAIL_ROUTE
import com.example.rss.presentation.navigation.SETTINGS_ROUTE
import com.example.rss.presentation.viewmodel.ArticleFilter
import com.example.rss.presentation.viewmodel.ArticleViewModel
import com.example.rss.presentation.viewmodel.RssSourceViewModel

private const val CATEGORY_PREFS = "rss_categories_prefs"
private const val CATEGORY_KEY = "custom_categories"

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    rssSourceViewModel: RssSourceViewModel = hiltViewModel(),
    articleViewModel: ArticleViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val rssSources by rssSourceViewModel.rssSources.collectAsStateCompat()
    val isLoading by rssSourceViewModel.isLoading.collectAsStateCompat()
    val pagingArticles = articleViewModel.articles.collectAsLazyPagingItems()

    var sidebarExpanded by remember { mutableStateOf(false) }
    var selectedSourceId by remember { mutableStateOf<Long?>(null) }
    var selectedCategoryName by remember { mutableStateOf("全部文章") }
    var searchQuery by remember { mutableStateOf("") }
    var sourceToDelete by remember { mutableStateOf<RssSource?>(null) }
    var sourceToMove by remember { mutableStateOf<RssSource?>(null) }
    var categoryToRename by remember { mutableStateOf<String?>(null) }
    var createCategoryDialog by remember { mutableStateOf(false) }
    var categoryInput by remember { mutableStateOf("") }

    val categoryExpanded = remember { mutableStateMapOf<String, Boolean>() }
    val customCategories = remember { mutableStateListOf<String>() }

    LaunchedEffect(Unit) {
        val saved = context.getSharedPreferences(CATEGORY_PREFS, 0)
            .getString(CATEGORY_KEY, "")
            .orEmpty()
            .split("||")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        customCategories.clear()
        customCategories.addAll(saved)

        rssSourceViewModel.loadRssSources()
        articleViewModel.setFilter(ArticleFilter.ALL)
        articleViewModel.loadArticles(null)
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = {
            if (selectedSourceId != null) {
                rssSourceViewModel.refreshRssSource(selectedSourceId!!)
                articleViewModel.loadArticles(selectedSourceId)
            } else {
                rssSourceViewModel.refreshAllSources()
                articleViewModel.loadArticles(null)
            }
        }
    )

    val grouped = rssSources.groupBy { it.category.ifBlank { "未分类" } }
    val allCategories = (listOf("全部文章", "未读", "收藏", "稍后读") + customCategories + grouped.keys)
        .distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RSS Reader") },
                navigationIcon = {
                    IconButton(onClick = { sidebarExpanded = !sidebarExpanded }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Menu,
                            contentDescription = "展开或折叠侧边栏",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (selectedSourceId != null) {
                                rssSourceViewModel.refreshRssSource(selectedSourceId!!)
                                articleViewModel.loadArticles(selectedSourceId)
                            } else {
                                rssSourceViewModel.refreshAllSources()
                                articleViewModel.loadArticles(null)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Refresh,
                            contentDescription = "刷新当前 RSS 源"
                        )
                    }
                    IconButton(onClick = { navController.navigate(SETTINGS_ROUTE) }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Settings,
                            contentDescription = "打开设置"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (sidebarExpanded) {
                Column(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("订阅管理", style = MaterialTheme.typography.titleMedium)
                        TextButton(onClick = { createCategoryDialog = true }) { Text("新建分类") }
                    }
                    TextButton(onClick = { navController.navigate(ADD_RSS_SOURCE_ROUTE) }) { Text("+ 添加 RSS 源") }
                    Divider()
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allCategories) { category ->
                            val isSystem = category in listOf("全部文章", "未读", "收藏", "稍后读")
                            val expanded = categoryExpanded[category] ?: true
                            CategoryRow(
                                name = category,
                                selected = selectedCategoryName == category,
                                expanded = expanded,
                                canRename = !isSystem,
                                onClick = {
                                    selectedCategoryName = category
                                    selectedSourceId = null
                                    when (category) {
                                        "全部文章" -> articleViewModel.setFilter(ArticleFilter.ALL)
                                        "未读" -> articleViewModel.setFilter(ArticleFilter.UNREAD)
                                        "收藏" -> articleViewModel.setFilter(ArticleFilter.FAVORITE)
                                        "稍后读" -> articleViewModel.setFilter(ArticleFilter.ALL)
                                        else -> articleViewModel.setFilter(ArticleFilter.CATEGORY)
                                    }
                                    if (category in grouped.keys) {
                                        articleViewModel.loadArticles(null)
                                    } else {
                                        articleViewModel.loadArticles(null)
                                    }
                                },
                                onToggle = { categoryExpanded[category] = !expanded },
                                onRename = { categoryToRename = category; categoryInput = category }
                            )
                            if (expanded && category !in listOf("全部文章", "未读", "收藏", "稍后读")) {
                                val sources = grouped[category].orEmpty()
                                sources.forEach { source ->
                                    SourceRow(
                                        source = source,
                                        selected = selectedSourceId == source.id,
                                        onClick = {
                                            selectedCategoryName = category
                                            selectedSourceId = source.id
                                            articleViewModel.setFilter(ArticleFilter.ALL)
                                            articleViewModel.loadArticles(source.id)
                                        },
                                        onMove = { sourceToMove = source },
                                        onDelete = { sourceToDelete = source }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxSize()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        if (it.isBlank()) {
                            articleViewModel.loadArticles(selectedSourceId)
                        } else {
                            articleViewModel.searchArticles(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    singleLine = true,
                    label = { Text("搜索标题/关键词") }
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    if (pagingArticles.itemCount == 0) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("暂无文章")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                count = pagingArticles.itemCount,
                                key = pagingArticles.itemKey { it.id }
                            ) { index ->
                                val article = pagingArticles[index] ?: return@items
                                val dismissState = rememberDismissState(
                                    confirmStateChange = { value ->
                                        if (value == DismissValue.DismissedToStart) {
                                            articleViewModel.deleteArticleById(article.id)
                                            Toast.makeText(context, "文章已删除", Toast.LENGTH_SHORT).show()
                                            true
                                        } else {
                                            false
                                        }
                                    }
                                )
                                SwipeToDismiss(
                                    state = dismissState,
                                    directions = setOf(DismissDirection.EndToStart),
                                    background = {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFFFFECEA)),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "删除文章")
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }
                                    },
                                    dismissContent = {
                                        ArticleCard(
                                            article = article,
                                            onOpen = { navController.navigate("$ARTICLE_DETAIL_ROUTE/${article.id}") },
                                            onToggleFavorite = { articleViewModel.toggleFavorite(article.id) }
                                        )
                                    }
                                )
                            }
                        }
                    }
                    PullRefreshIndicator(
                        refreshing = isLoading,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }

    if (sourceToDelete != null) {
        AlertDialog(
            onDismissRequest = { sourceToDelete = null },
            title = { Text("删除订阅源") },
            text = { Text("将删除该订阅源及其全部文章，是否继续？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val source = sourceToDelete ?: return@TextButton
                        articleViewModel.deleteArticlesBySource(source.id)
                        rssSourceViewModel.deleteRssSource(source)
                        if (selectedSourceId == source.id) {
                            selectedSourceId = null
                            selectedCategoryName = "全部文章"
                            articleViewModel.setFilter(ArticleFilter.ALL)
                            articleViewModel.loadArticles(null)
                        }
                        sourceToDelete = null
                    }
                ) { Text("确认删除") }
            },
            dismissButton = { TextButton(onClick = { sourceToDelete = null }) { Text("取消") } }
        )
    }

    if (sourceToMove != null) {
        AlertDialog(
            onDismissRequest = { sourceToMove = null },
            title = { Text("移动订阅源到分类") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    allCategories.filter { it !in listOf("全部文章", "未读", "收藏", "稍后读") }.forEach { name ->
                        Text(
                            text = name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val source = sourceToMove ?: return@clickable
                                    rssSourceViewModel.updateRssSource(source.copy(category = name))
                                    sourceToMove = null
                                }
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { sourceToMove = null }) { Text("关闭") } }
        )
    }

    if (createCategoryDialog) {
        AlertDialog(
            onDismissRequest = { createCategoryDialog = false },
            title = { Text("新建分类") },
            text = {
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("分类名称") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val name = categoryInput.trim()
                    if (name.isNotBlank() && !customCategories.contains(name)) {
                        customCategories.add(name)
                        context.getSharedPreferences(CATEGORY_PREFS, 0)
                            .edit()
                            .putString(CATEGORY_KEY, customCategories.joinToString("||"))
                            .apply()
                    }
                    categoryInput = ""
                    createCategoryDialog = false
                }) { Text("保存") }
            },
            dismissButton = {
                TextButton(onClick = {
                    categoryInput = ""
                    createCategoryDialog = false
                }) { Text("取消") }
            }
        )
    }

    if (categoryToRename != null) {
        AlertDialog(
            onDismissRequest = { categoryToRename = null },
            title = { Text("编辑分类名称") },
            text = {
                OutlinedTextField(
                    value = categoryInput,
                    onValueChange = { categoryInput = it },
                    label = { Text("新名称") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val old = categoryToRename ?: return@TextButton
                    val new = categoryInput.trim()
                    if (new.isNotBlank() && old != new) {
                        rssSources.filter { it.category == old }.forEach { source ->
                            rssSourceViewModel.updateRssSource(source.copy(category = new))
                        }
                        if (customCategories.contains(old)) {
                            customCategories.remove(old)
                        }
                        if (!customCategories.contains(new)) {
                            customCategories.add(new)
                        }
                        context.getSharedPreferences(CATEGORY_PREFS, 0)
                            .edit()
                            .putString(CATEGORY_KEY, customCategories.joinToString("||"))
                            .apply()
                    }
                    categoryToRename = null
                    categoryInput = ""
                }) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { categoryToRename = null }) { Text("取消") } }
        )
    }
}

@Composable
private fun CategoryRow(
    name: String,
    selected: Boolean,
    expanded: Boolean,
    canRename: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onRename: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (expanded) "▼" else "▶",
            modifier = Modifier.clickable { onToggle() }.padding(horizontal = 6.dp)
        )
        Text(
            text = name,
            modifier = Modifier.weight(1f).clickable { onClick() },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
        if (canRename) {
            IconButton(onClick = onRename, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                    contentDescription = "编辑分类"
                )
            }
        }
    }
}

@Composable
private fun SourceRow(
    source: RssSource,
    selected: Boolean,
    onClick: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp)
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 4.dp, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = source.getDisplayTitle(),
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onMove, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.DriveFileMove,
                contentDescription = "移动到其他分类"
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.Delete,
                contentDescription = "删除 RSS 源",
                tint = Color(0xFFD32F2F)
            )
        }
    }
}

@Composable
private fun ArticleCard(
    article: Article,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onOpen() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.sourceTitle.ifBlank { "未知来源" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = article.getFormattedDate(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = article.description.ifBlank { article.content.ifBlank { article.title } },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onToggleFavorite) {
                    Text(if (article.isFavorite) "取消收藏" else "收藏")
                }
                TextButton(onClick = {
                    if (article.articleUrl.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.articleUrl))
                        context.startActivity(intent)
                    }
                }) {
                    Text("查看原文")
                }
            }
        }
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsStateCompat():
    androidx.compose.runtime.State<T> = androidx.compose.runtime.collectAsState(this)
