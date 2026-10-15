package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.data.model.LedTheme
import com.example.ui.theme.ShareTechMonoFontFamily

@Composable
fun LedDigitalText(
    text: String,
    ghostMask: String = text.replace(Regex("[0-9]"), "8"),
    fontSize: TextUnit = 32.sp,
    theme: LedTheme = LedTheme.RED,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    hasGlow: Boolean = true
) {
    val activeColor = Color(theme.primaryHex)
    val dimColor = Color(theme.dimHex)
    val glowColor = Color(theme.glowHex).copy(alpha = 0.75f)

    Box(modifier = modifier) {
        // Ghosted unlit background segments (e.g. "88:88:88")
        Text(
            text = ghostMask,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            color = dimColor,
            textAlign = textAlign,
            letterSpacing = 1.sp
        )

        // Subtle glow diffusion behind the active digits
        if (hasGlow) {
            Text(
                text = text,
                fontFamily = ShareTechMonoFontFamily,
                fontSize = fontSize,
                fontWeight = FontWeight.Normal,
                color = glowColor.copy(alpha = 0.35f),
                textAlign = textAlign,
                letterSpacing = 1.sp,
                style = TextStyle(
                    shadow = Shadow(
                        color = glowColor,
                        offset = Offset(0f, 0f),
                        blurRadius = 14f
                    )
                )
            )
        }

        // Crisp brightly-lit active red LED segments
        Text(
            text = text,
            fontFamily = ShareTechMonoFontFamily,
            fontSize = fontSize,
            fontWeight = FontWeight.Normal,
            color = activeColor,
            textAlign = textAlign,
            letterSpacing = 1.sp,
            style = TextStyle(
                shadow = if (hasGlow) Shadow(
                    color = glowColor.copy(alpha = 0.6f),
                    offset = Offset(0f, 0f),
                    blurRadius = 6f
                ) else null
            )
        )
    }
}
