package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.os.VibrationEffect
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.AdhanSound
import com.example.data.model.AppLanguage
import com.example.data.model.PrayerType
import com.example.data.preferences.AppPreferences

class AdhanPlaybackService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    companion object {
        const val ACTION_PLAY_ADHAN = "com.example.ACTION_PLAY_ADHAN"
        const val ACTION_STOP_ADHAN = "com.example.ACTION_STOP_ADHAN"
        const val ACTION_PREVIEW_ADHAN = "com.example.ACTION_PREVIEW_ADHAN"

        const val EXTRA_PRAYER_CODE = "extra_prayer_code"
        const val EXTRA_SOUND_CODE = "extra_sound_code"

        private const val PLAYBACK_CHANNEL_ID = "noortime_adhan_playback"
        private const val PLAYBACK_NOTIF_ID = 3001

        fun startAdhan(context: Context, prayer: PrayerType) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PLAY_ADHAN
                putExtra(EXTRA_PRAYER_CODE, prayer.code)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun previewAdhan(context: Context, sound: AdhanSound) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_PREVIEW_ADHAN
                putExtra(EXTRA_SOUND_CODE, sound.code)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopAdhan(context: Context) {
            val intent = Intent(context, AdhanPlaybackService::class.java).apply {
                action = ACTION_STOP_ADHAN
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY

        if (action == ACTION_STOP_ADHAN) {
            stopPlayback()
            stopSelf()
            return START_NOT_STICKY
        }

        val prefs = AppPreferences(this)
        val isUrdu = prefs.language == AppLanguage.URDU

        handlePlayback(intent, prefs, isUrdu)

        return START_NOT_STICKY
    }

    private fun handlePlayback(intent: Intent, prefs: AppPreferences, isUrdu: Boolean) {
        val action = intent.action
        val isPreview = action == ACTION_PREVIEW_ADHAN

        val prayerCode = intent.getStringExtra(EXTRA_PRAYER_CODE)
        val prayer = prayerCode?.let { code -> PrayerType.values().find { it.code == code } }

        val soundCode = intent.getStringExtra(EXTRA_SOUND_CODE)
        val selectedSound = if (soundCode != null) {
            AdhanSound.values().find { it.code == soundCode } ?: prefs.selectedAdhanSound
        } else {
            prefs.selectedAdhanSound
        }

        val title = if (isPreview) {
            if (isUrdu) "آڈیو ٹیسٹ: ${selectedSound.urduName}" else "Testing Audio: ${selectedSound.englishName}"
        } else {
            val prayerName = prayer?.let { if (isUrdu) it.urduName else it.englishName } ?: "Prayer"
            if (isUrdu) "اذان جاری ہے: $prayerName" else "Adhan Playing: $prayerName"
        }

        val body = if (isUrdu) "آڈیو بند کرنے کے لیے یہاں ٹیپ کریں" else "Tap stop to silence the Adhan alert"

        val stopIntent = Intent(this, StopAdhanReceiver::class.java).apply {
            this.action = ACTION_STOP_ADHAN
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java).apply {
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = createNotification(title, body, openPendingIntent, stopPendingIntent, isUrdu)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(PLAYBACK_NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(PLAYBACK_NOTIF_ID, notification)
        }

        // Start playing the audio
        playAudio(selectedSound, prefs.adhanVolume)
    }

    private fun createNotification(
        title: String,
        body: String,
        openPendingIntent: PendingIntent,
        stopPendingIntent: PendingIntent,
        isUrdu: Boolean
    ): android.app.Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PLAYBACK_CHANNEL_ID,
                "Adhan Audio Playback",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Foreground playback for prayer time Adhan sounds"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, PLAYBACK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openPendingIntent)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_pause,
                if (isUrdu) "اذان بند کریں" else "Stop Adhan",
                stopPendingIntent
            )
            .build()
    }

    private fun playAudio(sound: AdhanSound, volume: Float) {
        stopPlayback()

        // Trigger vibration
        val pattern = longArrayOf(0, 400, 200, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }

        if (sound == AdhanSound.VIBRATE_ONLY) {
            // Nothing more to play
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return
        }

        try {
            val player = MediaPlayer()
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            player.setAudioAttributes(attributes)

            var dataSourceSet = false
            if (sound.rawResName != null) {
                val resId = resources.getIdentifier(sound.rawResName, "raw", packageName)
                if (resId != 0) {
                    val afd = resources.openRawResourceFd(resId)
                    player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    dataSourceSet = true
                }
            }

            if (!dataSourceSet) {
                val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (alertUri != null) {
                    player.setDataSource(this, alertUri)
                }
            }

            val clampedVolume = volume.coerceIn(0.1f, 1.0f)
            player.setVolume(clampedVolume, clampedVolume)

            player.setOnCompletionListener {
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }

            player.setOnErrorListener { _, _, _ ->
                stopPlayback()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                true
            }

            player.prepare()
            player.start()
            mediaPlayer = player

        } catch (e: Exception) {
            e.printStackTrace()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopPlayback() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        vibrator?.cancel()
    }

    override fun onDestroy() {
        stopPlayback()
        super.onDestroy()
    }
}
