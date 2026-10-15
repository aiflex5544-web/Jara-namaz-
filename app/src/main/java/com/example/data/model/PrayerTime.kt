package com.example.data.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

enum class PrayerType(val code: String, val englishName: String, val urduName: String) {
    FAJR("fajr", "Fajr", "فجر"),
    SUNRISE("sunrise", "Sunrise", "طلوع آفتاب"),
    DHUHR("dhuhr", "Dhuhr", "ظہر"),
    ASR("asr", "Asr", "عصر"),
    MAGHRIB("maghrib", "Maghrib", "مغرب"),
    ISHA("isha", "Isha", "عشاء");

    fun getDisplayName(language: AppLanguage): String {
        return if (language == AppLanguage.URDU) urduName else englishName
    }
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    URDU("ur", "Urdu", "اردو")
}

enum class LedTheme(
    val code: String,
    val displayName: String,
    val primaryHex: Long,
    val dimHex: Long,
    val glowHex: Long,
    val borderHex: Long
) {
    RED("red", "Red LED", 0xFFFF2B2B, 0xFF350606, 0xFFFF6060, 0xFF500A0A),
    GREEN("green", "Green LED", 0xFF00FF66, 0xFF003816, 0xFF66FF99, 0xFF0A4F23),
    BLUE("blue", "Blue LED", 0xFF00E5FF, 0xFF002F3E, 0xFF80EFFF, 0xFF06475C),
    WHITE("white", "White LED", 0xFFF5F7FA, 0xFF2A2E35, 0xFFFFFFFF, 0xFF3D4450)
}

enum class CalculationMethod(
    val code: String,
    val englishName: String,
    val urduName: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    KARACHI("karachi", "Univ. of Islamic Sciences, Karachi", "جامعہ علوم اسلامیہ، کراچی", 18.0, 18.0),
    MWL("mwl", "Muslim World League (MWL)", "رابطہ عالم اسلامی", 18.0, 17.0),
    ISNA("isna", "Islamic Society of N. America (ISNA)", "اسلامک سوسائٹی آف نارتھ امریکہ", 15.0, 15.0),
    UMM_AL_QURA("umm_al_qura", "Umm al-Qura University, Makkah", "جامعہ ام القریٰ، مکہ مکرمہ", 18.5, 0.0, ishaIntervalMinutes = 90),
    EGYPT("egypt", "Egyptian General Authority", "مصری جنرل اتھارٹی آف سروے", 19.5, 17.5),
    DUBAI("dubai", "Gulf / Dubai (90 min Isha)", "خلیج / دبئی", 18.5, 0.0, ishaIntervalMinutes = 90)
}

enum class JuristicMethod(val code: String, val englishName: String, val urduName: String, val shadowFactor: Double) {
    HANAFI("hanafi", "Hanafi (Standard Subcontinent)", "حنفی (دو مثل)", 2.0),
    STANDARD("standard", "Shafi'i, Maliki, Hanbali", "شافعی، مالکی، حنبلی (مثل)", 1.0)
}

enum class AdhanSound(
    val code: String,
    val englishName: String,
    val urduName: String,
    val rawResName: String?
) {
    TAKBEER("takbeer", "Adhan Takbeer (Allahu Akbar)", "اذان تکبیر (اللہ اکبر)", "adhan_takbeer"),
    FAJR("fajr", "Fajr Melodic Adhan", "فجر لحن اذان", "adhan_fajr"),
    CHIME("chime", "Soft Mosque Chime", "مسجد نرم چائم", "adhan_chime"),
    SYSTEM_DEFAULT("system", "Default Notification Tone", "سسٹم نوٹیفکیشن ٹون", null),
    VIBRATE_ONLY("vibrate", "Vibrate Only (Silent)", "صرف وائبریشن (خاموش)", null)
}

data class CityInfo(
    val id: String,
    val name: String,
    val urduName: String,
    val country: String,
    val countryUrdu: String,
    val latitude: Double,
    val longitude: Double,
    val timeZoneId: String
)

data class PrayerSchedule(
    val date: LocalDate,
    val hijriDay: Int,
    val hijriMonthName: String,
    val hijriMonthNameUrdu: String,
    val hijriYear: Int,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val dhuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime,
    val isFriday: Boolean
) {
    fun getTimeForPrayer(prayer: PrayerType): LocalTime {
        return when (prayer) {
            PrayerType.FAJR -> fajr
            PrayerType.SUNRISE -> sunrise
            PrayerType.DHUHR -> dhuhr
            PrayerType.ASR -> asr
            PrayerType.MAGHRIB -> maghrib
            PrayerType.ISHA -> isha
        }
    }

    fun getHijriFormatted(language: AppLanguage): String {
        val month = if (language == AppLanguage.URDU) hijriMonthNameUrdu else hijriMonthName
        val suffix = if (language == AppLanguage.URDU) "ھ" else "AH"
        return "$hijriDay $month $hijriYear $suffix"
    }
}

data class NextPrayerCountdown(
    val activePrayer: PrayerType?,
    val nextPrayer: PrayerType,
    val targetTime: LocalDateTime,
    val totalWindowSeconds: Long,
    val secondsRemaining: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long,
    val progress: Float
) {
    val formattedCountdown: String
        get() = String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
