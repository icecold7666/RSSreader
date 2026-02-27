package com.example.rss

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.navigation.compose.rememberNavController
import com.example.rss.presentation.navigation.NavigationGraph
import com.example.rss.presentation.navigation.SEARCH_ROUTE
import org.junit.Rule
import org.junit.Test

class NavigationUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun navigationGraph_canNavigateToSearchScreen() {
        composeRule.setContent {
            val navController = rememberNavController()
            NavigationGraph(navController = navController)
            LaunchedEffect(Unit) {
                navController.navigate(SEARCH_ROUTE)
            }
        }

        composeRule.onNodeWithText("搜索").assertIsDisplayed()
    }
}
