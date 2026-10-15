package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.calculator.PrayerTimeCalculator
import com.example.data.model.AppLanguage
import com.example.data.model.PrayerType
import com.example.data.preferences.AppPreferences
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object PrayerNotificationService {

    private const val PERSISTENT_CHANNEL_ID = "noortime_persistent_channel"
    private const val PERSISTENT_NOTIFICATION_ID = 1001

    fun updatePersistentNotification(context: Context) {
        val prefs = AppPreferences(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (!prefs.isPersistentNotificationEnabled) {
            nm.cancel(PERSISTENT_NOTIFICATION_ID)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PERSISTENT_CHANNEL_ID,
                "Live Prayer Times & Countdown",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mosque digital clock countdown and daily prayer schedule on lock screen"
                setShowBadge(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }

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
            PrayerType.SUNRISE to schedule.sunrise,
            PrayerType.DHUHR to schedule.dhuhr,
            PrayerType.ASR to schedule.asr,
            PrayerType.MAGHRIB to schedule.maghrib,
            PrayerType.ISHA to schedule.isha
        )

        // Find next prayer
        var nextPrayer = prayers.firstOrNull { it.second.isAfter(now) }
        val targetDateTime: LocalDateTime
        if (nextPrayer != null) {
            targetDateTime = LocalDateTime.of(today, nextPrayer.second)
        } else {
            // Next is tomorrow Fajr
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
        val secondsDiff = ChronoUnit.SECONDS.between(nowDateTime, targetDateTime).coerceAtLeast(0)
        val hours = secondsDiff / 3600
        val minutes = (secondsDiff % 3600) / 60
        val seconds = secondsDiff % 60
        val countdownStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val isUrdu = prefs.language == AppLanguage.URDU
        val nextName = if (isUrdu) nextPrayer.first.urduName else nextPrayer.first.englishName
        val nextTimeStr = nextPrayer.second.format(DateTimeFormatter.ofPattern("hh:mm a"))

        val title = if (isUrdu) {
            "اگلی نماز: $nextName ($nextTimeStr) • باقی $countdownStr"
        } else {
            "Next: $nextName ($nextTimeStr) • in $countdownStr"
        }

        val body = if (isUrdu) {
            "فجر ${schedule.fajr} • ظہر ${schedule.dhuhr} • عصر ${schedule.asr} • مغرب ${schedule.maghrib} • عشاء ${schedule.isha}"
        } else {
            "Fajr ${schedule.fajr} • Dhuhr ${schedule.dhuhr} • Asr ${schedule.asr} • Maghrib ${schedule.maghrib} • Isha ${schedule.isha}"
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, PERSISTENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setSubText(prefs.customCityName + " • " + schedule.getHijriFormatted(prefs.language))
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .build()

        nm.notify(PERSISTENT_NOTIFICATION_ID, notification)
    }

    fun scheduleUpcomingAlarms(context: Context) {
        val prefs = AppPreferences(context)
        val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val zoneId = runCatching { ZoneId.of(prefs.timeZoneId) }.getOrDefault(ZoneId.systemDefault())

        val today = LocalDate.now(zoneId)
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

        val prayers = listOf(
            PrayerType.FAJR to schedule.fajr,
            PrayerType.DHUHR to schedule.dhuhr,
            PrayerType.ASR to schedule.asr,
            PrayerType.MAGHRIB to schedule.maghrib,
            PrayerType.ISHA to schedule.isha
        )

        val now = ZonedDateTime.now(zoneId)

        prayers.forEachIndexed { index, (prayer, time) ->
            if (prefs.isAlertEnabled(prayer)) {
                var prayerTime = today.atTime(time).atZone(zoneId)
                if (prayerTime.isBefore(now)) {
                    // Schedule for tomorrow
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
                    prayerTime = today.plusDays(1).atTime(tomorrowSchedule.getTimeForPrayer(prayer)).atZone(zoneId)
                }

                val intent = PrayerAlarmReceiver.createAlarmIntent(context, prayer)
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    prayer.ordinal,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerMillis = prayerTime.toInstant().toEpochMilli()
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                    } else {
                        am.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                    }
                } catch (_: SecurityException) {
                    am.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            }
        }
    }
}
