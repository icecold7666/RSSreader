package com.example.rss

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.rss.domain.model.Article
import com.example.rss.presentation.ui.components.ArticleItem
import org.junit.Rule
import org.junit.Test

class ArticleItemUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun articleItem_showsTitleAndSource() {
        val article = Article(
            id = 1L,
            sourceId = 1L,
            sourceTitle = "测试源",
            title = "测试文章标题",
            description = "测试描述",
            content = "测试正文",
            articleUrl = "https://example.com/a",
            publishedDate = System.currentTimeMillis(),
            hash = "h"
        )

        composeRule.setContent {
            ArticleItem(
                article = article,
                onArticleClick = {},
                onToggleFavorite = {},
                onMarkAsRead = {}
            )
        }

        composeRule.onNodeWithText("测试文章标题").assertIsDisplayed()
        composeRule.onNodeWithText("测试源").assertIsDisplayed()
    }
}
