package com.example.rss.presentation.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PixelRssIcon16(tint: Color) = PixelIcon16(
    pixels = listOf(
        2 to 12, 3 to 12, 4 to 12, 5 to 12,
        2 to 9, 2 to 10, 2 to 11,
        4 to 10, 5 to 9, 6 to 8,
        6 to 12,
        8 to 8, 9 to 7, 10 to 6, 11 to 5,
        8 to 12,
        10 to 12, 11 to 12, 12 to 12
    ),
    tint = tint
)

@Composable
fun PixelRssIcon32(tint: Color) = PixelIcon32(
    pixels = listOf(
        6 to 24, 7 to 24, 8 to 24, 9 to 24, 10 to 24, 11 to 24,
        6 to 20, 6 to 21, 6 to 22, 6 to 23,
        10 to 20, 11 to 19, 12 to 18, 13 to 17,
        14 to 24, 15 to 24,
        18 to 18, 19 to 17, 20 to 16, 21 to 15,
        18 to 24, 19 to 24,
        22 to 24, 23 to 24, 24 to 24, 25 to 24
    ),
    tint = tint
)

@Composable
fun PixelMenuIcon16(tint: Color) = PixelIcon16(
    pixels = (3..12).flatMap { x -> listOf(x to 4, x to 7, x to 10) },
    tint = tint
)

@Composable
fun PixelBackIcon16(tint: Color) = PixelIcon16(
    pixels = listOf(
        4 to 8, 5 to 7, 5 to 9, 6 to 6, 6 to 10, 7 to 5, 7 to 11
    ) + (8..12).flatMap { x -> listOf(x to 8) },
    tint = tint
)

@Composable
fun PixelRefreshIcon16(tint: Color) = PixelIcon16(
    pixels = listOf(
        4 to 6, 5 to 5, 6 to 4, 7 to 4, 8 to 4, 9 to 5,
        10 to 6, 10 to 7, 9 to 8, 8 to 9, 7 to 9, 6 to 9, 5 to 8,
        10 to 4, 11 to 4, 11 to 5
    ),
    tint = tint
)

@Composable
fun PixelStarIcon16(tint: Color) = PixelIcon16(
    pixels = listOf(
        8 to 3, 8 to 4, 8 to 5,
        6 to 6, 7 to 6, 8 to 6, 9 to 6, 10 to 6,
        5 to 8, 6 to 8, 7 to 8, 8 to 8, 9 to 8, 10 to 8, 11 to 8,
        6 to 10, 7 to 10, 9 to 10, 10 to 10,
        7 to 11, 9 to 11
    ),
    tint = tint
)

@Composable
fun PixelDeleteIcon16(tint: Color) = PixelIcon16(
    pixels = listOf(
        5 to 4, 6 to 4, 7 to 4, 8 to 4, 9 to 4, 10 to 4,
        4 to 5, 11 to 5,
        5 to 6, 5 to 7, 5 to 8, 5 to 9, 5 to 10,
        10 to 6, 10 to 7, 10 to 8, 10 to 9, 10 to 10,
        6 to 10, 7 to 10, 8 to 10, 9 to 10
    ),
    tint = tint
)

@Composable
fun PixelSettingsIcon16(tint: Color) = PixelIcon16(
    pixels = listOf(
        8 to 3, 8 to 4, 5 to 5, 6 to 5, 10 to 5, 11 to 5,
        4 to 8, 5 to 8, 6 to 8, 10 to 8, 11 to 8, 12 to 8,
        5 to 11, 6 to 11, 10 to 11, 11 to 11, 8 to 12
    ),
    tint = tint
)

@Composable
private fun PixelIcon16(
    pixels: List<Pair<Int, Int>>,
    tint: Color
) {
    Canvas(modifier = Modifier.size(16.dp)) {
        val cell = size.minDimension / 16f
        pixels.forEach { (x, y) ->
            drawRect(
                color = tint,
                topLeft = Offset(x * cell, y * cell),
                size = Size(cell, cell)
            )
        }
    }
}

@Composable
private fun PixelIcon32(
    pixels: List<Pair<Int, Int>>,
    tint: Color
) {
    Canvas(modifier = Modifier.size(32.dp)) {
        val cell = size.minDimension / 32f
        pixels.forEach { (x, y) ->
            drawRect(
                color = tint,
                topLeft = Offset(x * cell, y * cell),
                size = Size(cell, cell)
            )
        }
    }
}
