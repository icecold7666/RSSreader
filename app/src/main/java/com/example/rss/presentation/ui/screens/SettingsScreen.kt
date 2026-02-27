package com.example.rss.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.rss.presentation.navigation.ADD_RSS_SOURCE_ROUTE
import com.example.rss.presentation.ui.icons.PixelBackIcon16
import com.example.rss.presentation.ui.theme.PixelColors
import com.example.rss.presentation.ui.theme.PixelSpacing
import com.example.rss.presentation.ui.theme.PixelTextStyles

@Composable
fun SettingsScreen(navController: NavController) {
    var autoRefresh by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.AppBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(PixelColors.ContentHeader)
                .padding(horizontal = PixelSpacing.s8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelActionButton(
                color = Color(0xFF1A1A1A),
                pressedColor = Color(0xFF111111),
                contentDescription = "返回主页",
                onClick = { navController.popBackStack() }
            ) {
                PixelBackIcon16(tint = Color.White)
            }
            Spacer(modifier = Modifier.width(PixelSpacing.s8))
            Text("Settings", style = PixelTextStyles.header, color = Color.White)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PixelSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(PixelSpacing.s16)
        ) {
            SettingSwitchRow(
                title = "自动刷新",
                checked = autoRefresh,
                onCheckedChange = { autoRefresh = it }
            )
            PixelWideButton(
                text = "添加 RSS 源",
                color = PixelColors.ActionGreen,
                pressedColor = PixelColors.ActionGreenPressed,
                contentDescription = "打开添加 RSS 源页面",
                onClick = { navController.navigate(ADD_RSS_SOURCE_ROUTE) }
            )
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PixelColors.CardBackground)
            .padding(PixelSpacing.s16),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = PixelTextStyles.sidebarItem, color = PixelColors.TextPrimary)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun PixelWideButton(
    text: String,
    color: Color,
    pressedColor: Color,
    contentDescription: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(if (pressed) pressedColor else color)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = PixelTextStyles.sidebarItem, color = Color.Black)
    }
}

@Composable
private fun PixelActionButton(
    color: Color,
    pressedColor: Color,
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    Box(
        modifier = Modifier
            .size(32.dp)
            .background(if (pressed) pressedColor else color)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
