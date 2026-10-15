package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppLanguage
import com.example.data.model.CityInfo
import com.example.data.model.LedTheme
import com.example.data.model.PrayerType
import com.example.data.repository.PrayerRepository
import com.example.data.repository.PrayerUiState
import com.example.service.PrayerNotificationService
import com.example.widget.NoorTimeWidgetProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = PrayerRepository(application)
    val uiState: StateFlow<PrayerUiState> = repository.uiState
    val prefs = repository.prefs

    init {
        // Schedule upcoming alarms and update lockscreen notification & widget
        PrayerNotificationService.scheduleUpcomingAlarms(application)
        PrayerNotificationService.updatePersistentNotification(application)
        NoorTimeWidgetProvider.updateAllWidgets(application)

        // Live 1-second countdown ticker
        viewModelScope.launch {
            var notifUpdateCounter = 0
            while (isActive) {
                delay(1000)
                repository.tickSecond()

                // Update notification every 60 seconds or when near a prayer change
                notifUpdateCounter++
                if (notifUpdateCounter >= 60) {
                    notifUpdateCounter = 0
                    PrayerNotificationService.updatePersistentNotification(getApplication())
                    NoorTimeWidgetProvider.updateAllWidgets(getApplication())
                }
            }
        }
    }

    fun selectTheme(theme: LedTheme) {
        repository.setTheme(theme)
    }

    fun toggleLanguage() {
        val nextLang = if (uiState.value.language == AppLanguage.URDU) AppLanguage.ENGLISH else AppLanguage.URDU
        repository.setLanguage(nextLang)
    }

    fun toggleAlert(prayer: PrayerType) {
        val current = prefs.isAlertEnabled(prayer)
        prefs.setAlertEnabled(prayer, !current)
        PrayerNotificationService.scheduleUpcomingAlarms(getApplication())
        repository.refreshState()
    }

    fun isAlertEnabled(prayer: PrayerType): Boolean {
        return prefs.isAlertEnabled(prayer)
    }

    fun selectCity(city: CityInfo) {
        repository.selectCity(city)
    }

    fun setFullscreen(fullscreen: Boolean) {
        repository.setFullscreenMode(fullscreen)
    }

    fun setSettingsOpen(open: Boolean) {
        repository.setSettingsOpen(open)
    }

    fun onSettingsChanged() {
        repository.refreshState()
        PrayerNotificationService.scheduleUpcomingAlarms(getApplication())
        PrayerNotificationService.updatePersistentNotification(getApplication())
        NoorTimeWidgetProvider.updateAllWidgets(getApplication())
    }

    fun refresh() {
        repository.refreshState()
        PrayerNotificationService.scheduleUpcomingAlarms(getApplication())
        PrayerNotificationService.updatePersistentNotification(getApplication())
        NoorTimeWidgetProvider.updateAllWidgets(getApplication())
    }
}
