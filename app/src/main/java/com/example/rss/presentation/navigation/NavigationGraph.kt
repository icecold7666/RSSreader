package com.example.rss.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.rss.presentation.ui.screens.ArticleDetailScreen
import com.example.rss.presentation.ui.screens.ArticleListScreen
import com.example.rss.presentation.ui.screens.AddRssSourceScreen
import com.example.rss.presentation.ui.screens.FavoritesScreen
import com.example.rss.presentation.ui.screens.MainScreen
import com.example.rss.presentation.ui.screens.RSSListScreen
import com.example.rss.presentation.ui.screens.SearchScreen
import com.example.rss.presentation.ui.screens.SettingsScreen

const val RSS_SOURCES_ROUTE = "rss_sources"
const val RSS_SOURCES_ADD_ROUTE = "rss_sources/add"
const val ADD_RSS_SOURCE_ROUTE = "add_rss_source"
const val SETTINGS_ROUTE = "settings"
const val ARTICLE_LIST_ROUTE = "article_list"
const val ARTICLE_DETAIL_ROUTE = "article_detail"
const val SEARCH_ROUTE = "search"
const val FAVORITES_ROUTE = "favorites"

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String = RSS_SOURCES_ROUTE
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(RSS_SOURCES_ROUTE) {
            MainScreen(navController = navController)
        }

        composable(ADD_RSS_SOURCE_ROUTE) {
            AddRssSourceScreen(navController = navController)
        }

        composable(SETTINGS_ROUTE) {
            SettingsScreen(navController = navController)
        }

        composable(RSS_SOURCES_ADD_ROUTE) {
            RSSListScreen(
                navController = navController,
                openAddDialogOnStart = true
            )
        }

        composable(
            route = "$ARTICLE_LIST_ROUTE?sourceId={sourceId}",
            arguments = listOf(navArgument("sourceId") {
                type = NavType.LongType
                defaultValue = -1L
            })
        ) { entry ->
            val raw = entry.arguments?.getLong("sourceId") ?: -1L
            val sourceId = if (raw <= 0L) null else raw
            ArticleListScreen(navController = navController, sourceId = sourceId)
        }

        composable(
            route = "$ARTICLE_DETAIL_ROUTE/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.LongType })
        ) { entry ->
            val articleId = entry.arguments?.getLong("articleId") ?: 0L
            ArticleDetailScreen(articleId = articleId, navController = navController)
        }

        composable(SEARCH_ROUTE) {
            SearchScreen(navController = navController)
        }

        composable(FAVORITES_ROUTE) {
            FavoritesScreen(navController = navController)
        }
    }
}
