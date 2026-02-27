package com.example.rss.presentation.ui.screens

import android.content.Intent
import android.text.Html
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.rss.domain.model.Article
import com.example.rss.presentation.ui.icons.PixelBackIcon16
import com.example.rss.presentation.ui.icons.PixelRefreshIcon16
import com.example.rss.presentation.ui.icons.PixelStarIcon16
import com.example.rss.presentation.ui.theme.PixelColors
import com.example.rss.presentation.ui.theme.PixelSpacing
import com.example.rss.presentation.ui.theme.PixelTextStyles
import com.example.rss.presentation.viewmodel.ArticleViewModel
import com.example.rss.presentation.viewmodel.RssSourceViewModel
import kotlinx.coroutines.launch

@Composable
fun ArticleDetailScreen(
    articleId: Long,
    navController: NavController,
    viewModel: ArticleViewModel = hiltViewModel(),
    rssSourceViewModel: RssSourceViewModel = hiltViewModel()
) {
    var article by remember { mutableStateOf<Article?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var fontScale by remember { mutableFloatStateOf(1.0f) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val loadArticle: () -> Unit = {
        scope.launch {
            isLoading = true
            article = viewModel.getArticleById(articleId)
            article?.let { viewModel.markAsRead(it.id) }
            isLoading = false
        }
    }

    LaunchedEffect(articleId) { loadArticle() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PixelColors.ContentBackground)
    ) {
        DetailTopBar(
            title = article?.sourceTitle?.ifBlank { "文章详情" } ?: "文章详情",
            onBack = { navController.popBackStack() },
            onRefresh = {
                article?.let {
                    rssSourceViewModel.refreshRssSource(it.sourceId)
                    loadArticle()
                }
            },
            onToggleFavorite = {
                article?.let {
                    viewModel.toggleFavorite(it.id)
                    article = it.copy(isFavorite = !it.isFavorite)
                }
            },
            onFontMinus = { if (fontScale > 0.8f) fontScale -= 0.1f },
            onFontPlus = { if (fontScale < 1.8f) fontScale += 0.1f },
            onShare = {
                article?.let {
                    val shareText = buildString {
                        append(it.title)
                        append("\n\n")
                        append(buildArticleBody(it))
                        append("\n\n")
                        append(it.articleUrl)
                    }
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "分享文章"))
                }
            },
            isFavorite = article?.isFavorite == true
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PixelColors.ActionCyan)
            }
            return@Column
        }

        val current = article
        if (current == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("文章不存在", style = PixelTextStyles.sidebarItem, color = PixelColors.TextPrimary)
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(PixelSpacing.s16)
        ) {
            Text(
                text = current.title,
                style = PixelTextStyles.header.copy(
                    fontSize = (18 * fontScale).sp,
                    lineHeight = (24 * fontScale).sp
                ),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(PixelSpacing.s8))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = current.sourceTitle.ifBlank { "未知来源" },
                    style = PixelTextStyles.sidebarItem.copy(fontSize = (12 * fontScale).sp),
                    color = PixelColors.TextSecondary
                )
                Text(
                    text = current.getFormattedDate(),
                    style = PixelTextStyles.sidebarItem.copy(fontSize = (12 * fontScale).sp),
                    color = PixelColors.ArticleMeta
                )
            }
            Spacer(modifier = Modifier.height(PixelSpacing.s16))
            Text(
                text = buildArticleBody(current),
                style = PixelTextStyles.articleSummary.copy(
                    fontSize = (15 * fontScale).sp,
                    lineHeight = (24 * fontScale).sp,
                    fontWeight = FontWeight.Normal
                ),
                color = PixelColors.TextPrimary
            )
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: () -> Unit,
    onFontMinus: () -> Unit,
    onFontPlus: () -> Unit,
    onShare: () -> Unit,
    isFavorite: Boolean
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
            contentDescription = "返回上一页",
            onClick = onBack
        ) { PixelBackIcon16(tint = Color.Black) }
        Spacer(modifier = Modifier.width(PixelSpacing.s8))
        Text(
            text = title,
            style = PixelTextStyles.header,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        PixelActionButton(
            color = PixelColors.ActionCyan,
            pressedColor = PixelColors.ActionCyanPressed,
            contentDescription = "刷新当前 RSS 源",
            onClick = onRefresh
        ) { PixelRefreshIcon16(tint = Color.Black) }
        Spacer(modifier = Modifier.width(PixelSpacing.s8))
        PixelActionButton(
            color = PixelColors.ActionYellow,
            pressedColor = PixelColors.ActionYellowPressed,
            contentDescription = if (isFavorite) "取消收藏文章" else "收藏文章",
            onClick = onToggleFavorite
        ) { PixelStarIcon16(tint = Color.Black) }
        Spacer(modifier = Modifier.width(PixelSpacing.s8))
        PixelActionButton(
            color = PixelColors.CardBackground,
            pressedColor = PixelColors.CardPressed,
            contentDescription = "减小字体",
            onClick = onFontMinus
        ) {
            Text("A-", style = PixelTextStyles.badge, color = PixelColors.TextPrimary)
        }
        Spacer(modifier = Modifier.width(PixelSpacing.s8))
        PixelActionButton(
            color = PixelColors.CardBackground,
            pressedColor = PixelColors.CardPressed,
            contentDescription = "增大字体",
            onClick = onFontPlus
        ) {
            Text("A+", style = PixelTextStyles.badge, color = PixelColors.TextPrimary)
        }
        Spacer(modifier = Modifier.width(PixelSpacing.s8))
        PixelActionButton(
            color = PixelColors.ActionCyan,
            pressedColor = PixelColors.ActionCyanPressed,
            contentDescription = "分享文章链接",
            onClick = onShare
        ) {
            Text("SH", style = PixelTextStyles.badge, color = Color.Black)
        }
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

private fun buildArticleBody(article: Article): String {
    val content = cleanHtml(article.content)
    val description = cleanHtml(article.description)
    if (content.isBlank() && description.isBlank()) return "暂无正文内容"
    if (content.isBlank()) return description
    if (description.isBlank()) return content
    if (content.contains(description)) return content
    if (description.contains(content)) return description
    return "$content\n\n$description"
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}

private fun cleanHtml(input: String): String {
    return Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY)
        .toString()
        .replace('\u00A0', ' ')
        .replace(Regex("[\\t\\x0B\\f\\r ]+"), " ")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}
