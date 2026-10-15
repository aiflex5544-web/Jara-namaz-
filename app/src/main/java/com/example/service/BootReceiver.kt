package com.example.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.widget.NoorTimeWidgetProvider

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED ||
            action == Intent.ACTION_DATE_CHANGED
        ) {
            PrayerNotificationService.scheduleUpcomingAlarms(context)
            PrayerNotificationService.updatePersistentNotification(context)
            NoorTimeWidgetProvider.updateAllWidgets(context)
        }
    }
}
