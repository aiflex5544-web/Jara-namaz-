package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.model.PrayerType
import com.example.data.preferences.AppPreferences
import com.example.widget.NoorTimeWidgetProvider

class PrayerAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PRAYER_ALERT = "com.example.ACTION_PRAYER_ALERT"
        const val EXTRA_PRAYER_CODE = "extra_prayer_code"

        fun createAlarmIntent(context: Context, prayer: PrayerType): Intent {
            return Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = ACTION_PRAYER_ALERT
                putExtra(EXTRA_PRAYER_CODE, prayer.code)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val prayerCode = intent?.getStringExtra(EXTRA_PRAYER_CODE) ?: return
        val prayer = PrayerType.values().find { it.code == prayerCode } ?: return
        val prefs = AppPreferences(context)

        // Only alert if enabled for this prayer
        if (!prefs.isAlertEnabled(prayer)) return

        // Launch the customizable Adhan sound alert notification service
        AdhanPlaybackService.startAdhan(context, prayer)

        // Also trigger widget and persistent countdown notification updates
        NoorTimeWidgetProvider.updateAllWidgets(context)
        PrayerNotificationService.updatePersistentNotification(context)
    }
}
