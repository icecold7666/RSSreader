package com.example.rss.presentation.ui.components

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.example.rss.domain.model.Article

/**
 * 文章项组件
 */
@Composable
fun ArticleItem(
    article: Article,
    onArticleClick: (Article) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onMarkAsRead: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Card(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .clickable { onArticleClick(article) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 收藏按钮
                Row(
                    modifier = Modifier
                        .align(Alignment.Top)
                        .padding(start = 8.dp)
                ) {
                    IconButton(onClick = { shareArticle(context, article) }) {
                        Icon(Icons.Default.Share, contentDescription = "分享")
                    }
                    IconButton(onClick = { onToggleFavorite(article.id) }) {
                        Icon(
                            imageVector = if (article.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (article.isFavorite) "已收藏" else "收藏",
                            tint = if (article.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 来源和时间
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.sourceTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = article.getFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 图片
            if (article.imageUrl?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(8.dp))
                val imageModel = remember(article.id, article.imageUrl) {
                    ImageRequest.Builder(context)
                        .data(article.imageUrl)
                        .memoryCacheKey("article_image_${article.id}")
                        .diskCacheKey(article.imageUrl ?: "article_image_${article.id}")
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .networkCachePolicy(CachePolicy.ENABLED)
                        .precision(Precision.INEXACT)
                        .crossfade(true)
                        .build()
                }
                AsyncImage(
                    model = imageModel,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // 描述
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.getShortDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // 作者
            if (article.author?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "作者: ${article.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 底部操作栏
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 已读/未读状态
                if (article.isRead) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "已读",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "已读",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 标记已读按钮
                if (!article.isRead) {
                    TextButton(
                        onClick = { onMarkAsRead(article.id) },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("标记已读")
                    }
                }
            }
        }
    }
}

/**
 * 文章项预览
 */
@Composable
fun ArticleItemPreview() {
    val previewArticle = Article(
        id = 1,
        sourceId = 1,
        sourceTitle = "示例RSS源",
        title = "这是一个示例文章标题，用于预览UI效果",
        description = "<p>这是文章的简短描述，用于预览文章项组件的显示效果。文章内容可能会很长，需要适当截断显示。</p>",
        content = "这是文章的完整内容...",
        articleUrl = "https://example.com/article/1",
        imageUrl = "https://example.com/image.jpg",
        publishedDate = System.currentTimeMillis() - 3600000, // 1小时前
        author = "作者姓名",
        isRead = false,
        isFavorite = true,
        hash = "abc123"
    )

    ArticleItem(
        article = previewArticle,
        onArticleClick = {},
        onToggleFavorite = {},
        onMarkAsRead = {}
    )
}

private fun shareArticle(context: android.content.Context, article: Article) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(Intent.EXTRA_TEXT, "${article.title}\n${article.articleUrl}")
    }
    context.startActivity(Intent.createChooser(shareIntent, "分享文章"))
}
