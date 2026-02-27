package com.example.rss.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.example.rss.presentation.ui.components.ArticleItem
import com.example.rss.presentation.viewmodel.ArticleFilter
import com.example.rss.presentation.viewmodel.ArticleViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun ArticleListScreen(
    navController: NavController,
    sourceId: Long? = null,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val filter by viewModel.filter.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val unreadCount = remember { mutableStateOf(0) }
    val favoriteCount = remember { mutableStateOf(0) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.loadArticles(sourceId) }
    )

    LaunchedEffect(sourceId) {
        viewModel.loadArticles(sourceId)
        unreadCount.value = viewModel.getUnreadCount(sourceId)
        favoriteCount.value = viewModel.getFavoriteCount()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(if (sourceId == null) "文章列表" else "RSS源文章") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("search") }) {
                        Icon(Icons.Default.Search, contentDescription = "搜索")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (sourceId != null && articles.itemCount > 0) {
                FloatingActionButton(onClick = { viewModel.markSourceAsRead(sourceId) }) {
                    Text("标记已读")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            FilterBar(
                selectedFilter = filter,
                onFilterChanged = viewModel::setFilter,
                unreadCount = unreadCount.value,
                favoriteCount = favoriteCount.value
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                when {
                    isLoading && articles.itemCount == 0 -> LoadingSkeletonList()
                    errorMessage != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(errorMessage ?: "加载失败", color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.clearError(); viewModel.loadArticles(sourceId) }) { Text("重试") }
                        }
                    }
                    articles.itemCount == 0 -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无文章，下拉刷新试试") }
                    else -> LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                        items(
                            count = articles.itemCount,
                            key = articles.itemKey { it.id },
                            contentType = articles.itemContentType { "article" }
                        ) { index ->
                            val article = articles[index] ?: return@items
                            ArticleItem(
                                article = article,
                                onArticleClick = { navController.navigate("article_detail/${it.id}") },
                                onToggleFavorite = viewModel::toggleFavorite,
                                onMarkAsRead = viewModel::markAsRead
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

@Composable
private fun LoadingSkeletonList() {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
        items(6) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(18.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            }
        }
    }
}

@Composable
fun FilterBar(
    selectedFilter: ArticleFilter,
    onFilterChanged: (ArticleFilter) -> Unit,
    unreadCount: Int,
    favoriteCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(selected = selectedFilter == ArticleFilter.ALL, onClick = { onFilterChanged(ArticleFilter.ALL) }, label = { Text("全部") })
        FilterChip(selected = selectedFilter == ArticleFilter.UNREAD, onClick = { onFilterChanged(ArticleFilter.UNREAD) }, label = { Text("未读 $unreadCount") })
        FilterChip(selected = selectedFilter == ArticleFilter.FAVORITE, onClick = { onFilterChanged(ArticleFilter.FAVORITE) }, label = { Text("收藏 $favoriteCount") })
    }
}
