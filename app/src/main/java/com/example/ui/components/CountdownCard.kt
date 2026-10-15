package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LedTheme
import com.example.data.model.NextPrayerCountdown
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CountdownCard(
    countdown: NextPrayerCountdown,
    theme: LedTheme,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(theme.primaryHex)
    val borderColor = Color(theme.borderHex)
    val dimColor = Color(theme.dimHex)
    val isUrdu = language == AppLanguage.URDU

    val nextPrayerName = if (isUrdu) countdown.nextPrayer.urduName else countdown.nextPrayer.englishName
    val activePrayerName = countdown.activePrayer?.let {
        if (isUrdu) it.urduName else it.englishName
    }

    val animatedProgress by animateFloatAsState(
        targetValue = countdown.progress,
        label = "countdown_progress"
    )

    val targetTimeStr = countdown.targetTime.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF000000))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: Next Prayer tag + Target Azan Time in LED styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(activeColor.copy(alpha = 0.18f))
                    .border(1.dp, activeColor.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HourglassTop,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUrdu) "اگلی نماز" else "NEXT PRAYER",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    letterSpacing = 1.sp
                )
            }

            // Target Prayer Time (Azan)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = activeColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = targetTimeStr,
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Next Prayer Name (Big Title)
        Text(
            text = nextPrayerName,
            fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
            fontSize = if (isUrdu) 34.sp else 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large Glowing Digital Countdown Display in Red LED
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF040404))
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LedDigitalText(
                    text = countdown.formattedCountdown,
                    ghostMask = "88:88:88",
                    fontSize = 46.sp,
                    theme = theme,
                    textAlign = TextAlign.Center,
                    hasGlow = true,
                    modifier = Modifier.testTag("countdown_timer_text")
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Countdown sub-labels: HRS | MINS | SECS
                Row(
                    modifier = Modifier.width(260.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isUrdu) "گھنٹے" else "HOURS",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isUrdu) "منٹ" else "MINS",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (isUrdu) "سیکنڈ" else "SECS",
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress bar for the current prayer window
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = activeColor,
            trackColor = dimColor
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom status info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activePrayerName != null) {
                    if (isUrdu) "حالیہ وقت: $activePrayerName" else "Current: $activePrayerName"
                } else {
                    if (isUrdu) "تہجد کا وقت" else "Tahajjud / Night"
                },
                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                fontSize = 12.sp,
                color = Color.LightGray.copy(alpha = 0.8f)
            )

            val remainingLabel = if (isUrdu) {
                "${countdown.hours} گھنٹے ${countdown.minutes} منٹ باقی"
            } else {
                "${countdown.hours}h ${countdown.minutes}m remaining"
            }
            Text(
                text = remainingLabel,
                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
        }
    }
}
