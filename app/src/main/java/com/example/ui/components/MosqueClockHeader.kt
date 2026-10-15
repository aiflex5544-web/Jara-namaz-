package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.PrayerSchedule
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.MosqueBlack
import com.example.ui.theme.MosqueCardDark
import com.example.ui.theme.ShareTechMonoFontFamily
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun MosqueClockHeader(
    currentTime: LocalTime,
    currentDate: LocalDate,
    schedule: PrayerSchedule,
    cityName: String,
    isAutoGps: Boolean,
    theme: LedTheme,
    language: AppLanguage,
    onCityClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(theme.primaryHex)
    val borderColor = Color(theme.borderHex)
    val dimColor = Color(theme.dimHex)

    val hour12 = if (currentTime.hour % 12 == 0) 12 else currentTime.hour % 12
    val hourStr = String.format(Locale.US, "%02d", hour12)
    val minuteStr = String.format(Locale.US, "%02d", currentTime.minute)
    val secondStr = String.format(Locale.US, "%02d", currentTime.second)
    val amPmStr = if (currentTime.hour < 12) "AM" else "PM"

    val isUrdu = language == AppLanguage.URDU

    // Blinking colon transition for authentic digital clock feeling
    val infiniteTransition = rememberInfiniteTransition(label = "clock_colon")
    val colonAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colon_blink"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF000000))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: App Branding, Mosque Digital Board badge, City Selector & Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Red LED Indicator Dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUrdu) "نور ٹائم" else "NOORTIME",
                    fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "•",
                    color = dimColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUrdu) "مسجد ڈیجیٹل کلاک" else "MOSQUE CLOCK",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            // Action Buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // City Pill Button
                Row(
                    modifier = Modifier
                        .testTag("city_selector_chip")
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0A0A0A))
                        .border(1.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .clickable { onCityClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isAutoGps) Icons.Default.MyLocation else Icons.Default.LocationOn,
                        contentDescription = "City Location",
                        tint = activeColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cityName,
                        fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onFullscreenClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("fullscreen_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Fullscreen Mosque Board",
                        tint = activeColor
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Arabic Calligraphy Greeting Banner
        Text(
            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
            fontFamily = AmiriFontFamily,
            fontSize = 16.sp,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large Mosque Digital Clock Screen (Main Red LED Display)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF040404))
                .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(vertical = 16.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Hours & Minutes Large LED Display
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LedDigitalText(
                        text = hourStr,
                        ghostMask = "88",
                        fontSize = 58.sp,
                        theme = theme,
                        textAlign = TextAlign.End
                    )

                    // Blinking colon
                    Box(modifier = Modifier.padding(horizontal = 2.dp)) {
                        Text(
                            text = ":",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 58.sp,
                            color = dimColor
                        )
                        Text(
                            text = ":",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 58.sp,
                            color = activeColor.copy(alpha = colonAlpha)
                        )
                    }

                    LedDigitalText(
                        text = minuteStr,
                        ghostMask = "88",
                        fontSize = 58.sp,
                        theme = theme,
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Seconds and AM/PM Column
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Large Seconds Display in Red LED
                    LedDigitalText(
                        text = secondStr,
                        ghostMask = "88",
                        fontSize = 26.sp,
                        theme = theme
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = amPmStr,
                        fontFamily = ShareTechMonoFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dates Row: Gregorian Date & Islamic Hijri Date in LED styling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gregorian Date
            val gregFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.getDefault())
            Text(
                text = currentDate.format(gregFormatter),
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp,
                color = Color.LightGray
            )

            // Hijri Date
            Text(
                text = schedule.getHijriFormatted(language),
                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                fontSize = if (isUrdu) 14.sp else 12.sp,
                color = activeColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
