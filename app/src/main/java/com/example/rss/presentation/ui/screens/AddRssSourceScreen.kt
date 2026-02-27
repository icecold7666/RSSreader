package com.example.rss.presentation.ui.screens

import android.util.Patterns
import android.widget.Toast
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rss.domain.model.RssSource
import com.example.rss.presentation.ui.icons.PixelBackIcon16
import com.example.rss.presentation.ui.theme.PixelColors
import com.example.rss.presentation.ui.theme.PixelSpacing
import com.example.rss.presentation.ui.theme.PixelTextStyles
import com.example.rss.presentation.viewmodel.RssSourceViewModel

@Composable
fun AddRssSourceScreen(
    navController: NavController,
    viewModel: RssSourceViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val enabled = title.isNotBlank() && url.isNotBlank()

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
                color = PixelColors.ActionGreen,
                pressedColor = PixelColors.ActionGreenPressed,
                contentDescription = "返回主页",
                onClick = { navController.popBackStack() }
            ) {
                PixelBackIcon16(tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(PixelSpacing.s8))
            Text("添加 RSS 源", style = PixelTextStyles.header, color = PixelColors.TextPrimary)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PixelSpacing.s16),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("标题") }
            )
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("URL") }
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                label = { Text("描述") }
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PixelWideButton(
                    text = "取消",
                    color = PixelColors.ActionRed,
                    pressedColor = PixelColors.ActionRedPressed,
                    contentDescription = "取消添加 RSS 源",
                    onClick = { navController.popBackStack() },
                    enabled = true
                )
                PixelWideButton(
                    text = "保存",
                    color = PixelColors.ActionGreen,
                    pressedColor = PixelColors.ActionGreenPressed,
                    contentDescription = "保存 RSS 源",
                    enabled = enabled,
                    onClick = {
                        if (!Patterns.WEB_URL.matcher(url.trim()).matches()) {
                            Toast.makeText(context, "URL 无效", Toast.LENGTH_SHORT).show()
                            return@PixelWideButton
                        }
                        viewModel.addRssSource(
                            RssSource(
                                title = title.trim(),
                                url = url.trim(),
                                description = description.trim()
                            )
                        )
                        Toast.makeText(context, "已添加", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
private fun PixelWideButton(
    text: String,
    color: Color,
    pressedColor: Color,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val bg = when {
        !enabled -> PixelColors.PressedGray
        pressed -> pressedColor
        else -> color
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(bg)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
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
