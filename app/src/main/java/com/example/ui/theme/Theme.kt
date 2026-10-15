package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.model.LedTheme

@Composable
fun NoorTimeTheme(
    ledTheme: LedTheme = LedTheme.RED,
    content: @Composable () -> Unit
) {
    val (primary, secondary, border) = when (ledTheme) {
        LedTheme.RED -> Triple(LedRedPrimary, LedRedGlow, LedRedBorder)
        LedTheme.GREEN -> Triple(LedGreenPrimary, LedGreenGlow, LedGreenBorder)
        LedTheme.BLUE -> Triple(LedBluePrimary, LedBlueGlow, LedBlueBorder)
        LedTheme.WHITE -> Triple(LedWhitePrimary, LedWhiteGlow, LedWhiteBorder)
    }

    val colorScheme = darkColorScheme(
        primary = primary,
        onPrimary = Color.Black,
        secondary = secondary,
        onSecondary = Color.Black,
        background = MosqueBlack,
        onBackground = Color.White,
        surface = MosqueCardDark,
        onSurface = Color.White,
        surfaceVariant = MosqueSurfaceVariant,
        onSurfaceVariant = MosqueTextDim,
        outline = border
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
