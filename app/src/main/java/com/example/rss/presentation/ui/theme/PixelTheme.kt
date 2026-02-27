package com.example.rss.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PixelColors {
    val AppBackground = Color(0xFF2C2C2C)
    val SidebarBackground = Color(0xFF000000)
    val ContentBackground = Color(0xFF2C2C2C)
    val ContentHeader = Color(0xFF383838)

    val SelectionBlue = Color(0xFF0099FF)
    val SelectionPressed = Color(0xFF0077CC)
    val PressedGray = Color(0xFF2A2A2A)

    val CardBackground = Color(0xFF444444)
    val CardPressed = Color(0xFF3A3A3A)

    val TextPrimary = Color(0xFFE0E0E0)
    val TextSecondary = Color(0xFFB0B0B0)
    val ArticleSummary = Color(0xFFB0B0B0)
    val ArticleMeta = Color(0xFF999999)

    val ActionGreen = Color(0xFF00CC00)
    val ActionGreenPressed = Color(0xFF009900)
    val ActionYellow = Color(0xFFFFCC00)
    val ActionYellowPressed = Color(0xFFCC9900)
    val ActionRed = Color(0xFFFF3300)
    val ActionRedPressed = Color(0xFFCC2200)
    val ActionCyan = Color(0xFF00CCCC)
    val ActionCyanPressed = Color(0xFF009999)

    val BadgeRed = Color(0xFFFF3300)
    val IconCyan = Color(0xFF00CCCC)
}

object PixelSpacing {
    val s8 = 8.dp
    val s16 = 16.dp
    val s24 = 24.dp
    val s32 = 32.dp
}

object PixelTextStyles {
    private val mono = FontFamily.Monospace

    val header = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
    val sidebarTitle = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
    val sidebarItem = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp
    )
    val badge = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 12.sp
    )
    val articleTitle = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
    val articleSummary = TextStyle(
        fontFamily = mono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}

@Composable
fun PixelTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        background = PixelColors.AppBackground,
        surface = PixelColors.CardBackground,
        primary = PixelColors.SelectionBlue,
        onPrimary = Color.White,
        onBackground = PixelColors.TextPrimary,
        onSurface = PixelColors.TextPrimary
    )
    val typography = Typography(
        titleLarge = PixelTextStyles.header,
        titleMedium = PixelTextStyles.sidebarTitle,
        bodyLarge = PixelTextStyles.sidebarItem,
        bodyMedium = PixelTextStyles.articleSummary
    )
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}
