package com.example.rss

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.rss.presentation.ui.components.AddRssSourceDialog
import org.junit.Rule
import org.junit.Test

class AddRssSourceDialogUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun addButton_enabledOnlyWhenRequiredFieldsPresent() {
        composeRule.setContent {
            AddRssSourceDialog(
                onDismiss = {},
                onConfirm = {}
            )
        }

        composeRule.onNodeWithText("添加").assertIsNotEnabled()

        composeRule.onNodeWithText("标题 *").performTextInput("测试源")
        composeRule.onNodeWithText("添加").assertIsNotEnabled()

        composeRule.onNodeWithText("URL *").performTextInput("https://example.com/rss")
        composeRule.onNodeWithText("添加").assertIsEnabled()
    }
}
