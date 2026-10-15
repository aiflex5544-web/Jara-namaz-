package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.calculator.PrayerTimeCalculator
import com.example.data.model.AppLanguage
import com.example.data.model.PrayerType
import com.example.data.preferences.AppPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NoorTimeWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (widgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, NoorTimeWidgetProvider::class.java)
            val ids = appWidgetManager.getAppWidgetIds(componentName)
            if (ids.isNotEmpty()) {
                val provider = NoorTimeWidgetProvider()
                provider.onUpdate(context, appWidgetManager, ids)
            }
        }

        private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
            val prefs = AppPreferences(context)
            val today = LocalDate.now()
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

            val now = LocalTime.now()
            val prayers = listOf(
                PrayerType.FAJR to schedule.fajr,
                PrayerType.DHUHR to schedule.dhuhr,
                PrayerType.ASR to schedule.asr,
                PrayerType.MAGHRIB to schedule.maghrib,
                PrayerType.ISHA to schedule.isha
            )

            var nextPrayer = prayers.firstOrNull { it.second.isAfter(now) }
            val targetDateTime: LocalDateTime
            if (nextPrayer != null) {
                targetDateTime = LocalDateTime.of(today, nextPrayer.second)
            } else {
                val tomorrowSchedule = PrayerTimeCalculator.calculate(
                    date = today.plusDays(1),
                    latitude = prefs.latitude,
                    longitude = prefs.longitude,
                    timeZoneId = prefs.timeZoneId,
                    method = prefs.calculationMethod,
                    juristic = prefs.juristicMethod,
                    minuteAdjustments = prefs.getAllMinuteAdjustments(),
                    hijriOffsetDays = prefs.hijriOffset
                )
                nextPrayer = PrayerType.FAJR to tomorrowSchedule.fajr
                targetDateTime = LocalDateTime.of(today.plusDays(1), tomorrowSchedule.fajr)
            }

            val nowDateTime = LocalDateTime.now()
            val secondsRemaining = ChronoUnit.SECONDS.between(nowDateTime, targetDateTime).coerceAtLeast(0)
            val hours = secondsRemaining / 3600
            val minutes = (secondsRemaining % 3600) / 60
            val seconds = secondsRemaining % 60
            val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

            val views = RemoteViews(context.packageName, R.layout.widget_noor_time)

            val isUrdu = prefs.language == AppLanguage.URDU
            val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

            views.setTextViewText(R.id.widget_city_name, prefs.customCityName)
            views.setTextViewText(
                R.id.widget_next_prayer_name,
                if (isUrdu) nextPrayer.first.urduName else nextPrayer.first.englishName
            )
            views.setTextViewText(R.id.widget_countdown_text, countdownStr)

            // Labels
            views.setTextViewText(R.id.widget_next_label, if (isUrdu) "اگلی نماز" else "NEXT PRAYER")
            views.setTextViewText(R.id.widget_countdown_label, if (isUrdu) "باقی وقت" else "TIME REMAINING")

            // Prayer times
            views.setTextViewText(R.id.widget_fajr_label, if (isUrdu) "فجر" else "FAJR")
            views.setTextViewText(R.id.widget_fajr_time, schedule.fajr.format(timeFormatter))

            views.setTextViewText(R.id.widget_dhuhr_label, if (isUrdu) "ظہر" else "DHUHR")
            views.setTextViewText(R.id.widget_dhuhr_time, schedule.dhuhr.format(timeFormatter))

            views.setTextViewText(R.id.widget_asr_label, if (isUrdu) "عصر" else "ASR")
            views.setTextViewText(R.id.widget_asr_time, schedule.asr.format(timeFormatter))

            views.setTextViewText(R.id.widget_maghrib_label, if (isUrdu) "مغرب" else "MAGHRIB")
            views.setTextViewText(R.id.widget_maghrib_time, schedule.maghrib.format(timeFormatter))

            views.setTextViewText(R.id.widget_isha_label, if (isUrdu) "عشاء" else "ISHA")
            views.setTextViewText(R.id.widget_isha_time, schedule.isha.format(timeFormatter))

            // Open app on click
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
}
