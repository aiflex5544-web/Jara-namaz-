package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AdhanSound
import com.example.data.model.AppLanguage
import com.example.data.model.CalculationMethod
import com.example.data.model.CityDirectory
import com.example.data.model.CityInfo
import com.example.data.model.JuristicMethod
import com.example.data.model.LedTheme
import com.example.data.model.PrayerType

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("noortime_app_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_USE_AUTO_GPS = "use_auto_gps"
        private const val KEY_CITY_ID = "selected_city_id"
        private const val KEY_CUSTOM_CITY_NAME = "custom_city_name"
        private const val KEY_LATITUDE = "custom_latitude"
        private const val KEY_LONGITUDE = "custom_longitude"
        private const val KEY_TIMEZONE = "custom_timezone"

        private const val KEY_THEME = "led_theme"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_CALC_METHOD = "calc_method"
        private const val KEY_JURISTIC_METHOD = "juristic_method"
        private const val KEY_HIJRI_OFFSET = "hijri_offset"

        private const val KEY_PERSISTENT_NOTIF = "persistent_notification_enabled"
        private const val KEY_FULLSCREEN_KEEP_ON = "keep_screen_on_fullscreen"

        private const val KEY_ADHAN_SOUND = "selected_adhan_sound"
        private const val KEY_ADHAN_VOLUME = "adhan_volume"

        private const val PREFIX_ALERT = "alert_enabled_"
        private const val PREFIX_OFFSET = "minute_offset_"
    }

    var useAutoGps: Boolean
        get() = prefs.getBoolean(KEY_USE_AUTO_GPS, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_AUTO_GPS, value).apply()

    var selectedCityId: String
        get() = prefs.getString(KEY_CITY_ID, CityDirectory.DEFAULT_CITY.id) ?: CityDirectory.DEFAULT_CITY.id
        set(value) = prefs.edit().putString(KEY_CITY_ID, value).apply()

    var customCityName: String
        get() = prefs.getString(KEY_CUSTOM_CITY_NAME, CityDirectory.DEFAULT_CITY.name) ?: CityDirectory.DEFAULT_CITY.name
        set(value) = prefs.edit().putString(KEY_CUSTOM_CITY_NAME, value).apply()

    var latitude: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_LATITUDE, java.lang.Double.doubleToRawLongBits(CityDirectory.DEFAULT_CITY.latitude))
        )
        set(value) = prefs.edit().putLong(KEY_LATITUDE, java.lang.Double.doubleToRawLongBits(value)).apply()

    var longitude: Double
        get() = java.lang.Double.longBitsToDouble(
            prefs.getLong(KEY_LONGITUDE, java.lang.Double.doubleToRawLongBits(CityDirectory.DEFAULT_CITY.longitude))
        )
        set(value) = prefs.edit().putLong(KEY_LONGITUDE, java.lang.Double.doubleToRawLongBits(value)).apply()

    var timeZoneId: String
        get() = prefs.getString(KEY_TIMEZONE, CityDirectory.DEFAULT_CITY.timeZoneId) ?: CityDirectory.DEFAULT_CITY.timeZoneId
        set(value) = prefs.edit().putString(KEY_TIMEZONE, value).apply()

    var ledTheme: LedTheme
        get() {
            val code = prefs.getString(KEY_THEME, LedTheme.RED.code)
            return LedTheme.values().find { it.code == code } ?: LedTheme.RED
        }
        set(value) = prefs.edit().putString(KEY_THEME, value.code).apply()

    var language: AppLanguage
        get() {
            val code = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code)
            return AppLanguage.values().find { it.code == code } ?: AppLanguage.ENGLISH
        }
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value.code).apply()

    var calculationMethod: CalculationMethod
        get() {
            val code = prefs.getString(KEY_CALC_METHOD, CalculationMethod.KARACHI.code)
            return CalculationMethod.values().find { it.code == code } ?: CalculationMethod.KARACHI
        }
        set(value) = prefs.edit().putString(KEY_CALC_METHOD, value.code).apply()

    var juristicMethod: JuristicMethod
        get() {
            val code = prefs.getString(KEY_JURISTIC_METHOD, JuristicMethod.HANAFI.code)
            return JuristicMethod.values().find { it.code == code } ?: JuristicMethod.HANAFI
        }
        set(value) = prefs.edit().putString(KEY_JURISTIC_METHOD, value.code).apply()

    var hijriOffset: Int
        get() = prefs.getInt(KEY_HIJRI_OFFSET, 0)
        set(value) = prefs.edit().putInt(KEY_HIJRI_OFFSET, value).apply()

    var isPersistentNotificationEnabled: Boolean
        get() = prefs.getBoolean(KEY_PERSISTENT_NOTIF, true)
        set(value) = prefs.edit().putBoolean(KEY_PERSISTENT_NOTIF, value).apply()

    var isKeepScreenOnInFullscreen: Boolean
        get() = prefs.getBoolean(KEY_FULLSCREEN_KEEP_ON, true)
        set(value) = prefs.edit().putBoolean(KEY_FULLSCREEN_KEEP_ON, value).apply()

    var selectedAdhanSound: AdhanSound
        get() {
            val code = prefs.getString(KEY_ADHAN_SOUND, AdhanSound.TAKBEER.code)
            return AdhanSound.values().find { it.code == code } ?: AdhanSound.TAKBEER
        }
        set(value) = prefs.edit().putString(KEY_ADHAN_SOUND, value.code).apply()

    var adhanVolume: Float
        get() = prefs.getFloat(KEY_ADHAN_VOLUME, 0.85f)
        set(value) = prefs.edit().putFloat(KEY_ADHAN_VOLUME, value).apply()

    fun isAlertEnabled(prayer: PrayerType): Boolean {
        return prefs.getBoolean(PREFIX_ALERT + prayer.code, true)
    }

    fun setAlertEnabled(prayer: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean(PREFIX_ALERT + prayer.code, enabled).apply()
    }

    fun getMinuteAdjustment(prayer: PrayerType): Int {
        return prefs.getInt(PREFIX_OFFSET + prayer.code, 0)
    }

    fun setMinuteAdjustment(prayer: PrayerType, offsetMinutes: Int) {
        prefs.edit().putInt(PREFIX_OFFSET + prayer.code, offsetMinutes).apply()
    }

    fun getAllMinuteAdjustments(): Map<PrayerType, Int> {
        return PrayerType.values().associateWith { getMinuteAdjustment(it) }
    }

    fun applyCity(city: CityInfo) {
        useAutoGps = false
        selectedCityId = city.id
        customCityName = city.name
        latitude = city.latitude
        longitude = city.longitude
        timeZoneId = city.timeZoneId
    }

    fun applyGpsCoordinates(lat: Double, lng: Double, detectedCityName: String, tz: String) {
        useAutoGps = true
        selectedCityId = "gps_custom"
        customCityName = detectedCityName
        latitude = lat
        longitude = lng
        timeZoneId = tz
    }
}
