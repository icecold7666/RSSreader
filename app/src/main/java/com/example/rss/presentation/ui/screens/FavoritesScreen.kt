package com.example.rss.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.example.rss.presentation.ui.components.ArticleItem
import com.example.rss.presentation.viewmodel.ArticleFilter
import com.example.rss.presentation.viewmodel.ArticleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel()
) {
    val favorites = viewModel.articles.collectAsLazyPagingItems()
    LaunchedEffect(Unit) {
        viewModel.setFilter(ArticleFilter.FAVORITE)
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("收藏") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        if (favorites.itemCount == 0) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) { Text("暂无收藏") }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(vertical = 8.dp)) {
                items(
                    count = favorites.itemCount,
                    key = favorites.itemKey { it.id }
                ) { index ->
                    val article = favorites[index] ?: return@items
                    ArticleItem(
                        article = article,
                        onArticleClick = { navController.navigate("article_detail/${it.id}") },
                        onToggleFavorite = viewModel::toggleFavorite,
                        onMarkAsRead = viewModel::markAsRead
                    )
                }
            }
        }
    }
}
