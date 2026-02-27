package com.example.rss.presentation.ui.screens

import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.RssFeed
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rss.domain.model.RssSource
import com.example.rss.presentation.navigation.ARTICLE_LIST_ROUTE
import com.example.rss.presentation.navigation.RSS_SOURCES_ADD_ROUTE
import com.example.rss.presentation.navigation.RSS_SOURCES_ROUTE
import com.example.rss.presentation.ui.components.AddRssSourceDialog
import com.example.rss.presentation.viewmodel.RssSourceViewModel

private const val TAG = "RSSListScreen"
private val BrandBlue = Color(0xFF4285F4)
private val BackgroundLight = Color(0xFFF8F9FA)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun RSSListScreen(
    navController: NavController,
    openAddDialogOnStart: Boolean = false,
    viewModel: RssSourceViewModel = hiltViewModel()
) {
    val rssSources by viewModel.rssSources.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(openAddDialogOnStart) }

    val backgroundColor = if (MaterialTheme.colorScheme.background == Color.Black) {
        MaterialTheme.colorScheme.background
    } else {
        BackgroundLight
    }
    val descColor = if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFFBBBBBB) else Color(0xFF666666)
    val timeColor = if (MaterialTheme.colorScheme.background == Color.Black) Color(0xFF8F8F8F) else Color(0xFF999999)

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refreshAllSources() }
    )

    LaunchedEffect(Unit) { viewModel.loadRssSources() }
    LaunchedEffect(openAddDialogOnStart) {
        if (openAddDialogOnStart) showAddDialog = true
    }
    LaunchedEffect(operationMessage) {
        val message = operationMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearOperationMessage()
        }
    }

    Scaffold(
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "AI RSS 源",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 30.sp,
                            letterSpacing = 0.5.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAllSources() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新", tint = BrandBlue)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    Log.d(TAG, "FAB clicked, navigate -> $RSS_SOURCES_ADD_ROUTE")
                    navController.navigate(RSS_SOURCES_ADD_ROUTE)
                },
                modifier = Modifier.size(56.dp),
                containerColor = BrandBlue,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 8.dp
                )
            ) {
                androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = "添加RSS源")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            if (rssSources.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(rssSources, key = { it.id }) { source ->
                        val dismissState = rememberDismissState(
                            confirmStateChange = { value ->
                                if (value == DismissValue.DismissedToStart) {
                                    viewModel.deleteRssSource(source)
                                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
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
                                        .background(Color(0xFFD32F2F))
                                        .padding(horizontal = 20.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "删除",
                                        tint = Color.White
                                    )
                                    Box(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "删除",
                                        color = Color.White,
                                        style = TextStyle(
                                            fontFamily = FontFamily.SansSerif,
                                            fontSize = 14.sp,
                                            lineHeight = 21.sp,
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                }
                            },
                            dismissContent = {
                                RssSourceCard(
                                    source = source,
                                    descriptionColor = descColor,
                                    timeColor = timeColor,
                                    onClick = {
                                        navController.navigate("$ARTICLE_LIST_ROUTE?sourceId=${source.id}")
                                    }
                                )
                            }
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = BrandBlue
            )
        }
    }

    if (showAddDialog) {
        AddRssSourceDialog(
            onDismiss = {
                showAddDialog = false
                if (openAddDialogOnStart) {
                    if (!navController.popBackStack()) {
                        navController.navigate(RSS_SOURCES_ROUTE)
                    }
                }
            },
            onConfirm = { source ->
                viewModel.addRssSource(source)
                showAddDialog = false
                if (openAddDialogOnStart) {
                    if (!navController.popBackStack()) {
                        navController.navigate(RSS_SOURCES_ROUTE)
                    }
                }
            }
        )
    }
}

@Composable
private fun RssSourceCard(
    source: RssSource,
    descriptionColor: Color,
    timeColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = source.getDisplayTitle(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 24.sp,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = source.description.ifBlank { source.url },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    letterSpacing = 0.5.sp
                ),
                color = descriptionColor,
                modifier = Modifier.padding(top = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "更新于 ${formatLastUpdate(source.lastUpdate)}",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = timeColor
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.RssFeed,
            contentDescription = null,
            tint = Color(0xFFB0B7C3),
            modifier = Modifier.size(42.dp)
        )
        Text(
            text = "暂无订阅",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color(0xFF666666),
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "点击 + 添加",
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                letterSpacing = 0.5.sp
            ),
            color = Color(0xFF999999),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

private fun formatLastUpdate(timestamp: Long): String {
    if (timestamp <= 0L) return "从未"
    return DateFormat.format("yyyy-MM-dd HH:mm", timestamp).toString()
}
