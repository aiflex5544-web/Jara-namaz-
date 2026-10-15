package com.example.ui.components

import android.app.Activity
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LedTheme
import com.example.data.model.NextPrayerCountdown
import com.example.data.model.PrayerSchedule
import com.example.data.model.PrayerType
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.MosqueBlack
import com.example.ui.theme.MosqueCardDark
import com.example.ui.theme.ShareTechMonoFontFamily
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FullScreenMosqueMode(
    currentTime: LocalTime,
    currentDate: LocalDate,
    schedule: PrayerSchedule,
    countdown: NextPrayerCountdown,
    cityName: String,
    theme: LedTheme,
    language: AppLanguage,
    onExit: () -> Unit
) {
    BackHandler { onExit() }

    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val activeColor = Color(theme.primaryHex)
    val borderColor = Color(theme.borderHex)
    val isUrdu = language == AppLanguage.URDU

    val hour12 = if (currentTime.hour % 12 == 0) 12 else currentTime.hour % 12
    val hourStr = String.format(Locale.US, "%02d", hour12)
    val minuteStr = String.format(Locale.US, "%02d", currentTime.minute)
    val secondStr = String.format(Locale.US, "%02d", currentTime.second)
    val amPmStr = if (currentTime.hour < 12) "AM" else "PM"

    val nextPrayerName = if (isUrdu) countdown.nextPrayer.urduName else countdown.nextPrayer.englishName

    val prayers = listOf(
        PrayerType.FAJR to schedule.fajr,
        PrayerType.SUNRISE to schedule.sunrise,
        PrayerType.DHUHR to schedule.dhuhr,
        PrayerType.ASR to schedule.asr,
        PrayerType.MAGHRIB to schedule.maghrib,
        PrayerType.ISHA to schedule.isha
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MosqueBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Close / Exit button
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .testTag("exit_fullscreen_button")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Exit Fullscreen",
                tint = Color.LightGray
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mosque Header & Dates
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                fontFamily = AmiriFontFamily,
                fontSize = 20.sp,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cityName,
                    fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                    fontSize = 14.sp,
                    color = activeColor
                )
                Text(
                    text = "  •  ${schedule.getHijriFormatted(language)}  •  ${currentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))}",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Mosque Digital Clock Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MosqueCardDark)
                    .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    LedDigitalText(
                        text = "$hourStr:$minuteStr",
                        ghostMask = "88:88",
                        fontSize = 62.sp,
                        theme = theme
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        LedDigitalText(
                            text = secondStr,
                            ghostMask = "88",
                            fontSize = 26.sp,
                            theme = theme
                        )
                        Text(
                            text = amPmStr,
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Countdown Panel
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MosqueCardDark)
                    .border(1.5.dp, activeColor.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isUrdu) "اگلی نماز" else "NEXT PRAYER",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = nextPrayerName,
                            fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isUrdu) "باقی وقت" else "TIME REMAINING",
                            fontFamily = ShareTechMonoFontFamily,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        LedDigitalText(
                            text = countdown.formattedCountdown,
                            ghostMask = "88:88:88",
                            fontSize = 28.sp,
                            theme = theme
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timetable Grid (3x2 or 2x3 depending on screen)
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(prayers) { (prayer, time) ->
                    val isNext = countdown.nextPrayer == prayer
                    val timeStr = time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNext) activeColor.copy(alpha = 0.15f) else MosqueCardDark)
                            .border(if (isNext) 1.5.dp else 1.dp, if (isNext) activeColor else borderColor, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isUrdu) prayer.urduName else prayer.englishName,
                                    fontFamily = if (isUrdu) AmiriFontFamily else ShareTechMonoFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNext) activeColor else Color.White
                                )
                                if (isNext) {
                                    Text(
                                        text = if (isUrdu) "اگلی" else "NEXT",
                                        fontFamily = ShareTechMonoFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LedDigitalText(
                                text = timeStr,
                                ghostMask = "88:88 88",
                                fontSize = 18.sp,
                                theme = theme
                            )
                        }
                    }
                }
            }
        }
    }
}
