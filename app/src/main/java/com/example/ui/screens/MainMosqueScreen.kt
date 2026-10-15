package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerUiState
import com.example.ui.components.CountdownCard
import com.example.ui.components.MosqueClockHeader
import com.example.ui.components.PrayerTimesTable
import com.example.ui.theme.AmiriFontFamily
import com.example.ui.theme.MosqueBlack
import com.example.ui.theme.MosqueCardDark
import com.example.ui.theme.ShareTechMonoFontFamily

@Composable
fun MainMosqueScreen(
    state: PrayerUiState,
    isAlertEnabled: (PrayerType) -> Boolean,
    onToggleAlert: (PrayerType) -> Unit,
    onSelectTheme: (LedTheme) -> Unit,
    onToggleLanguage: () -> Unit,
    onCityClick: () -> Unit,
    onFullscreenClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = Color(state.theme.primaryHex)
    val borderColor = Color(state.theme.borderHex)
    val isUrdu = state.language == AppLanguage.URDU

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MosqueBlack)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Digital Mosque Clock Header
        MosqueClockHeader(
            currentTime = state.currentTime,
            currentDate = state.currentDate,
            schedule = state.schedule,
            cityName = state.selectedCityName,
            isAutoGps = state.isAutoGps,
            theme = state.theme,
            language = state.language,
            onCityClick = onCityClick,
            onFullscreenClick = onFullscreenClick,
            onSettingsClick = onSettingsClick
        )

        // 2. Quick Control Bar: Theme switcher & Language toggle & Fullscreen trigger
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MosqueCardDark)
                .border(1.dp, borderColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Theme color dots
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (isUrdu) "تھیم:" else "THEME:",
                    fontFamily = ShareTechMonoFontFamily,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
                LedTheme.values().forEach { t ->
                    val isSelected = state.theme == t
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(t.primaryHex))
                            .border(
                                width = if (isSelected) 2.dp else 0.5.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onSelectTheme(t) }
                    )
                }
            }

            // Quick Language switch chip (EN / اردو)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(activeColor.copy(alpha = 0.15f))
                        .border(1.dp, activeColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .clickable { onToggleLanguage() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isUrdu) "English" else "اردو",
                        fontFamily = if (isUrdu) ShareTechMonoFontFamily else AmiriFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeColor
                    )
                }

                // Wall Clock Mode Quick Button
                IconButton(
                    onClick = onFullscreenClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Mosque Wall Board Mode",
                        tint = activeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Refresh Button
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh prayer times",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 3. Prominent Live Countdown to Next Prayer
        CountdownCard(
            countdown = state.countdown,
            theme = state.theme,
            language = state.language
        )

        // 4. Daily Prayer Times Table (Mosque LED Board style)
        PrayerTimesTable(
            schedule = state.schedule,
            countdown = state.countdown,
            theme = state.theme,
            language = state.language,
            isAlertEnabled = isAlertEnabled,
            onToggleAlert = onToggleAlert
        )

        // 5. Mosque Footer Notes (Subtle authentic mosque timetable metadata)
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isUrdu) {
                    "مسجد ڈیجیٹل اوقاتِ نماز • نور ٹائم"
                } else {
                    "NOORTIME • ISLAMIC PRAYER CLOCK & LIVE COUNTDOWN"
                },
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 10.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isUrdu) {
                    "حساب: ${state.juristicMethod.urduName} • آف لائن فل سپورٹ"
                } else {
                    "Calculation: ${state.juristicMethod.englishName} • 100% Offline"
                },
                fontFamily = ShareTechMonoFontFamily,
                fontSize = 9.sp,
                color = Color.DarkGray
            )
        }
    }
}
