package com.example.rss.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.rss.domain.model.RssSource

/**
 * 添加RSS源对话框
 */
@Composable
fun AddRssSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (RssSource) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("默认分类") }
    var customTitle by remember { mutableStateOf("") }
    var fetchInterval by remember { mutableStateOf("3600000") }
    var isLoading by remember { mutableStateOf(false) }

    // 预设分类
    val categories = listOf(
        "默认分类",
        "科技",
        "新闻",
        "博客",
        "娱乐",
        "体育",
        "财经",
        "其他"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "添加RSS源",
                        style = MaterialTheme.typography.titleLarge
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 表单
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("自定义标题（可选）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    var expanded by remember { mutableStateOf(false) }

                    Box(modifier = Modifier.fillMaxWidth(0.6f)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("分类") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isLoading,
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expanded = !expanded }) {
                                    Icon(Icons.Default.Add, contentDescription = "选择分类")
                                }
                            }
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = fetchInterval,
                        onValueChange = { fetchInterval = it },
                        label = { Text("刷新间隔(ms)") },
                        modifier = Modifier.fillMaxWidth(0.35f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = !isLoading
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("取消")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (title.isNotEmpty() && url.isNotEmpty()) {
                                isLoading = true
                                val source = RssSource(
                                    title = title,
                                    url = url,
                                    description = description,
                                    category = category,
                                    customTitle = customTitle.takeIf { it.isNotEmpty() },
                                    fetchInterval = fetchInterval.toLongOrNull() ?: 3600000
                                )
                                onConfirm(source)
                            }
                        },
                        enabled = !isLoading && title.isNotEmpty() && url.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("添加")
                        }
                    }
                }
            }
        }
    }
}