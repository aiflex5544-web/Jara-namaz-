package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LedTheme
import com.example.data.model.NextPrayerCountdown
import com.example.data.model.PrayerSchedule
import com.example.data.model.PrayerType
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.ShareTechMonoFontFamily
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun PrayerTimesTable(
    schedule: PrayerSchedule,
    countdown: NextPrayerCountdown,
    theme: LedTheme,
    language: AppLanguage,
    isAlertEnabled: (PrayerType) -> Boolean,
    onToggleAlert: (PrayerType) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(theme.primaryHex)
    val borderColor = Color(theme.borderHex)
    val dimColor = Color(theme.dimHex)
    val isUrdu = language == AppLanguage.URDU

    val rows = listOf(
        PrayerType.FAJR to schedule.fajr,
        PrayerType.SUNRISE to schedule.sunrise,
        PrayerType.DHUHR to schedule.dhuhr,
        PrayerType.ASR to schedule.asr,
        PrayerType.MAGHRIB to schedule.maghrib,
        PrayerType.ISHA to schedule.isha
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF000000))
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Table Header in Mosque LED Board Style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1.1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(activeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isUrdu) "نماز" else "PRAYER",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = if (isUrdu) "اذان / وقت" else "AZAN / TIME",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = activeColor,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1.5f)
            )

            Text(
                text = if (isUrdu) "حیثیت / الارم" else "STATUS / ALERT",
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.weight(1.1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Prayer Rows with Large Red LED-Style Typography
        rows.forEach { (prayer, time) ->
            val isNext = countdown.nextPrayer == prayer
            val isActive = countdown.activePrayer == prayer
            val isHighlighted = isNext || isActive
            val alertOn = isAlertEnabled(prayer)

            val rowBgColor by animateColorAsState(
                targetValue = if (isNext) activeColor.copy(alpha = 0.16f) else if (isActive) Color(0xFF140505) else Color(0xFF070707),
                label = "prayer_row_bg"
            )

            val rowBorderColor = if (isNext) activeColor else if (isActive) borderColor else Color(0xFF181818)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(rowBgColor)
                    .border(if (isHighlighted) 1.6.dp else 1.dp, rowBorderColor, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Prayer Name & Arabic Calligraphy
                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsating / Steady Red LED Indicator Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isNext) activeColor else if (isActive) activeColor.copy(alpha = 0.6f) else dimColor)
                            .border(1.dp, if (isHighlighted) activeColor else Color.Transparent, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = if (isUrdu) prayer.urduName else prayer.englishName,
                            fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                            fontSize = if (isUrdu) 20.sp else 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isNext) activeColor else Color.White
                        )
                        if (prayer == PrayerType.DHUHR && schedule.isFriday) {
                            Text(
                                text = if (isUrdu) "جمعہ المبارک" else "Jummah",
                                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD166),
                                fontWeight = FontWeight.Bold
                            )
                        } else if (prayer == PrayerType.SUNRISE) {
                            Text(
                                text = if (isUrdu) "اشراق" else "Ishraq",
                                fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // Column 2: Large Red LED-Style Typography for Prayer Time
                val timeStr = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
                Box(
                    modifier = Modifier.weight(1.5f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    LedDigitalText(
                        text = timeStr,
                        ghostMask = "88:88 88",
                        fontSize = 24.sp,
                        theme = theme,
                        hasGlow = isHighlighted
                    )
                }

                // Column 3: Status Badge & Alert Toggle Icon
                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    if (isNext) {
                        Text(
                            text = if (isUrdu) "اگلی" else "NEXT",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(activeColor.copy(alpha = 0.22f))
                                .border(1.dp, activeColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else if (isActive) {
                        Text(
                            text = if (isUrdu) "جاری" else "ACTIVE",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 10.sp,
                            color = Color.LightGray,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.DarkGray.copy(alpha = 0.4f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Alert Notification Toggle (Bell icon)
                    if (prayer != PrayerType.SUNRISE) {
                        IconButton(
                            onClick = { onToggleAlert(prayer) },
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("toggle_alert_${prayer.code}")
                        ) {
                            Icon(
                                imageVector = if (alertOn) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = "Toggle alert for ${prayer.englishName}",
                                tint = if (alertOn) activeColor else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
