package com.example.rss.presentation.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rss.presentation.navigation.ARTICLE_LIST_ROUTE
import com.example.rss.presentation.viewmodel.RssSourceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RssSourceScreen(
    navController: NavController,
    viewModel: RssSourceViewModel = hiltViewModel()
) {
    val rssSources by viewModel.rssSources.collectAsState()
    val operationMessage by viewModel.operationMessage.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.loadRssSources() }
    LaunchedEffect(operationMessage) {
        val message = operationMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearOperationMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            LargeTopAppBar(
                title = { Text("RSS 源") },
                actions = {
                    TextButton(onClick = { viewModel.clearCache(context) }) {
                        Text("清缓存")
                    }
                    TextButton(onClick = { viewModel.backupDatabase(context) }) {
                        Text("备份")
                    }
                    TextButton(onClick = { viewModel.exportToOpml(context) }) {
                        Text("导出")
                    }
                    TextButton(onClick = { viewModel.importFromOpml(context) }) {
                        Text("导入")
                    }
                    IconButton(onClick = { viewModel.refreshAllSources() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { }) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rssSources, key = { it.id }) { source ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("$ARTICLE_LIST_ROUTE?sourceId=${source.id}") }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(source.getDisplayTitle())
                    Text(source.url)
                }
            }
        }
    }
}
