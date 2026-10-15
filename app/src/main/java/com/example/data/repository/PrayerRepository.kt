package com.example.data.repository

import android.content.Context
import com.example.data.calculator.PrayerTimeCalculator
import com.example.data.model.AppLanguage
import com.example.data.model.CityDirectory
import com.example.data.model.CityInfo
import com.example.data.model.JuristicMethod
import com.example.data.model.LedTheme
import com.example.data.model.NextPrayerCountdown
import com.example.data.model.PrayerSchedule
import com.example.data.model.PrayerType
import com.example.data.preferences.AppPreferences
import com.example.service.PrayerNotificationService
import com.example.widget.NoorTimeWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

data class PrayerUiState(
    val selectedCityName: String,
    val isAutoGps: Boolean,
    val currentDate: LocalDate,
    val currentTime: LocalTime,
    val schedule: PrayerSchedule,
    val countdown: NextPrayerCountdown,
    val theme: LedTheme,
    val language: AppLanguage,
    val juristicMethod: JuristicMethod,
    val isFullscreenMode: Boolean = false,
    val isSettingsOpen: Boolean = false
)

class PrayerRepository(private val context: Context) {

    val prefs = AppPreferences(context)

    private val _uiState = MutableStateFlow(buildCurrentState())
    val uiState: StateFlow<PrayerUiState> = _uiState.asStateFlow()

    fun refreshState() {
        _uiState.value = buildCurrentState()
    }

    fun tickSecond() {
        val current = _uiState.value
        val nowTime = LocalTime.now()
        val nowDate = LocalDate.now()

        // Check if day changed
        if (nowDate != current.currentDate) {
            _uiState.value = buildCurrentState()
            PrayerNotificationService.scheduleUpcomingAlarms(context)
            NoorTimeWidgetProvider.updateAllWidgets(context)
            return
        }

        val countdown = calculateCountdown(nowDate, nowTime, current.schedule)
        _uiState.value = current.copy(
            currentTime = nowTime,
            countdown = countdown
        )
    }

    private fun buildCurrentState(): PrayerUiState {
        val today = LocalDate.now()
        val nowTime = LocalTime.now()

        val schedule = PrayerTimeCalculator.calculate(
            date = today,
            latitude = prefs.latitude,
            longitude = prefs.longitude,
            timeZoneId = prefs.timeZoneId,
            method = prefs.calculationMethod,
            juristic = prefs.juristicMethod,
            minuteAdjustments = prefs.getAllMinuteAdjustments(),
            hijriOffsetDays = prefs.hijriOffset
        )

        val countdown = calculateCountdown(today, nowTime, schedule)

        return PrayerUiState(
            selectedCityName = prefs.customCityName,
            isAutoGps = prefs.useAutoGps,
            currentDate = today,
            currentTime = nowTime,
            schedule = schedule,
            countdown = countdown,
            theme = prefs.ledTheme,
            language = prefs.language,
            juristicMethod = prefs.juristicMethod
        )
    }

    private fun calculateCountdown(
        date: LocalDate,
        now: LocalTime,
        schedule: PrayerSchedule
    ): NextPrayerCountdown {
        val prayerPairs = listOf(
            PrayerType.FAJR to schedule.fajr,
            PrayerType.SUNRISE to schedule.sunrise,
            PrayerType.DHUHR to schedule.dhuhr,
            PrayerType.ASR to schedule.asr,
            PrayerType.MAGHRIB to schedule.maghrib,
            PrayerType.ISHA to schedule.isha
        )

        // Find active prayer (last prayer whose time is <= now)
        val activePrayer = prayerPairs.lastOrNull { !now.isBefore(it.second) }?.first

        // Find next prayer (first prayer whose time is > now)
        val nextPair = prayerPairs.firstOrNull { now.isBefore(it.second) }

        val nextPrayer: PrayerType
        val targetDateTime: LocalDateTime
        val prevTargetDateTime: LocalDateTime

        if (nextPair != null) {
            nextPrayer = nextPair.first
            targetDateTime = LocalDateTime.of(date, nextPair.second)

            val prevIndex = prayerPairs.indexOfFirst { it.first == nextPrayer } - 1
            if (prevIndex >= 0) {
                prevTargetDateTime = LocalDateTime.of(date, prayerPairs[prevIndex].second)
            } else {
                // Before Fajr, previous prayer was yesterday's Isha
                val yesterdaySchedule = PrayerTimeCalculator.calculate(
                    date = date.minusDays(1),
                    latitude = prefs.latitude,
                    longitude = prefs.longitude,
                    timeZoneId = prefs.timeZoneId,
                    method = prefs.calculationMethod,
                    juristic = prefs.juristicMethod,
                    minuteAdjustments = prefs.getAllMinuteAdjustments(),
                    hijriOffsetDays = prefs.hijriOffset
                )
                prevTargetDateTime = LocalDateTime.of(date.minusDays(1), yesterdaySchedule.isha)
            }
        } else {
            // After Isha, next is tomorrow Fajr
            nextPrayer = PrayerType.FAJR
            val tomorrowSchedule = PrayerTimeCalculator.calculate(
                date = date.plusDays(1),
                latitude = prefs.latitude,
                longitude = prefs.longitude,
                timeZoneId = prefs.timeZoneId,
                method = prefs.calculationMethod,
                juristic = prefs.juristicMethod,
                minuteAdjustments = prefs.getAllMinuteAdjustments(),
                hijriOffsetDays = prefs.hijriOffset
            )
            targetDateTime = LocalDateTime.of(date.plusDays(1), tomorrowSchedule.fajr)
            prevTargetDateTime = LocalDateTime.of(date, schedule.isha)
        }

        val currentDateTime = LocalDateTime.of(date, now)
        val secondsRemaining = ChronoUnit.SECONDS.between(currentDateTime, targetDateTime).coerceAtLeast(0)
        val totalWindowSeconds = ChronoUnit.SECONDS.between(prevTargetDateTime, targetDateTime).coerceAtLeast(1)

        val progress = (1f - (secondsRemaining.toFloat() / totalWindowSeconds.toFloat())).coerceIn(0f, 1f)

        val hours = secondsRemaining / 3600
        val minutes = (secondsRemaining % 3600) / 60
        val seconds = secondsRemaining % 60

        return NextPrayerCountdown(
            activePrayer = activePrayer,
            nextPrayer = nextPrayer,
            targetTime = targetDateTime,
            totalWindowSeconds = totalWindowSeconds,
            secondsRemaining = secondsRemaining,
            hours = hours,
            minutes = minutes,
            seconds = seconds,
            progress = progress
        )
    }

    fun selectCity(city: CityInfo) {
        prefs.applyCity(city)
        refreshState()
        PrayerNotificationService.scheduleUpcomingAlarms(context)
        PrayerNotificationService.updatePersistentNotification(context)
        NoorTimeWidgetProvider.updateAllWidgets(context)
    }

    fun setGpsLocation(lat: Double, lng: Double, cityName: String, tz: String) {
        prefs.applyGpsCoordinates(lat, lng, cityName, tz)
        refreshState()
        PrayerNotificationService.scheduleUpcomingAlarms(context)
        PrayerNotificationService.updatePersistentNotification(context)
        NoorTimeWidgetProvider.updateAllWidgets(context)
    }

    fun setTheme(theme: LedTheme) {
        prefs.ledTheme = theme
        _uiState.value = _uiState.value.copy(theme = theme)
    }

    fun setLanguage(language: AppLanguage) {
        prefs.language = language
        _uiState.value = _uiState.value.copy(language = language)
        PrayerNotificationService.updatePersistentNotification(context)
        NoorTimeWidgetProvider.updateAllWidgets(context)
    }

    fun setFullscreenMode(fullscreen: Boolean) {
        _uiState.value = _uiState.value.copy(isFullscreenMode = fullscreen)
    }

    fun setSettingsOpen(open: Boolean) {
        _uiState.value = _uiState.value.copy(isSettingsOpen = open)
    }
}
