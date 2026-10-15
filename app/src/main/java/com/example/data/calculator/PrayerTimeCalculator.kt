package com.example.data.calculator

import com.example.data.model.CalculationMethod
import com.example.data.model.JuristicMethod
import com.example.data.model.PrayerSchedule
import com.example.data.model.PrayerType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.*

object PrayerTimeCalculator {

    private val HIJRI_MONTHS_EN = listOf(
        "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
        "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
        "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"
    )

    private val HIJRI_MONTHS_UR = listOf(
        "محرم الحرام", "صفر المظفر", "ربیع الاول", "ربیع الثانی",
        "جمادی الاول", "جمادی الثانی", "رجب المرجب", "شعبان المعظم",
        "رمضان المبارک", "شوال المکرم", "ذوالقعدۃ", "ذوالحجۃ"
    )

    fun calculate(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        timeZoneId: String,
        method: CalculationMethod,
        juristic: JuristicMethod,
        minuteAdjustments: Map<PrayerType, Int> = emptyMap(),
        hijriOffsetDays: Int = 0
    ): PrayerSchedule {
        val zoneId = runCatching { ZoneId.of(timeZoneId) }.getOrDefault(ZoneId.systemDefault())
        val zonedDateTime = date.atStartOfDay(zoneId)
        val tzOffsetHours = zoneId.rules.getOffset(zonedDateTime.toInstant()).totalSeconds / 3600.0

        val jd = julianDay(date.year, date.monthValue, date.dayOfMonth)

        // Solar noon, declination, and equation of time
        val solarCoords = calculateSolarCoordinates(jd)
        val eqt = solarCoords.equationOfTime
        val decl = solarCoords.declination

        // Solar noon time in hours
        val noon = 12.0 + tzOffsetHours - (longitude / 15.0) - eqt

        // Sun altitude angle for sunrise and sunset: 0.833 degrees refraction/radius
        val sunriseSunAngle = -0.8333

        // Fajr
        val fajrAngle = -method.fajrAngle
        val fajrHours = calculateSunHourAngle(latitude, decl, fajrAngle)?.let { noon - it } ?: (noon - 1.5)

        // Sunrise
        val sunriseHours = calculateSunHourAngle(latitude, decl, sunriseSunAngle)?.let { noon - it } ?: (noon - 1.0)

        // Dhuhr (solar noon + slight 1.5 min safe margin)
        val dhuhrHours = noon + (1.5 / 60.0)

        // Asr
        val asrAltitude = calculateAsrAltitude(latitude, decl, juristic.shadowFactor)
        val asrHours = calculateSunHourAngle(latitude, decl, asrAltitude)?.let { noon + it } ?: (noon + 3.0)

        // Maghrib / Sunset
        val sunsetHours = calculateSunHourAngle(latitude, decl, sunriseSunAngle)?.let { noon + it } ?: (noon + 6.0)

        // Isha
        val ishaHours = if (method.ishaIntervalMinutes != null) {
            sunsetHours + (method.ishaIntervalMinutes / 60.0)
        } else {
            val ishaAngle = -method.ishaAngle
            calculateSunHourAngle(latitude, decl, ishaAngle)?.let { noon + it } ?: (sunsetHours + 1.5)
        }

        // Apply minute adjustments
        val fajrTime = hoursToLocalTime(fajrHours, minuteAdjustments[PrayerType.FAJR] ?: 0)
        val sunriseTime = hoursToLocalTime(sunriseHours, minuteAdjustments[PrayerType.SUNRISE] ?: 0)
        val dhuhrTime = hoursToLocalTime(dhuhrHours, minuteAdjustments[PrayerType.DHUHR] ?: 0)
        val asrTime = hoursToLocalTime(asrHours, minuteAdjustments[PrayerType.ASR] ?: 0)
        val maghribTime = hoursToLocalTime(sunsetHours, minuteAdjustments[PrayerType.MAGHRIB] ?: 0)
        val ishaTime = hoursToLocalTime(ishaHours, minuteAdjustments[PrayerType.ISHA] ?: 0)

        val hijri = calculateHijriDate(jd + hijriOffsetDays)

        val isFriday = date.dayOfWeek == DayOfWeek.FRIDAY

        return PrayerSchedule(
            date = date,
            hijriDay = hijri.day,
            hijriMonthName = HIJRI_MONTHS_EN.getOrElse(hijri.month - 1) { "Hijri" },
            hijriMonthNameUrdu = HIJRI_MONTHS_UR.getOrElse(hijri.month - 1) { "ہجری" },
            hijriYear = hijri.year,
            fajr = fajrTime,
            sunrise = sunriseTime,
            dhuhr = dhuhrTime,
            asr = asrTime,
            maghrib = maghribTime,
            isha = ishaTime,
            isFriday = isFriday
        )
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private data class SolarCoordinates(val declination: Double, val equationOfTime: Double)

    private fun calculateSolarCoordinates(jd: Double): SolarCoordinates {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = fixAngle(
            Math.toDegrees(
                atan2(
                    cos(Math.toRadians(e)) * sin(Math.toRadians(l)),
                    cos(Math.toRadians(l))
                )
            )
        ) / 15.0

        val decl = Math.toDegrees(asin(sin(Math.toRadians(e)) * sin(Math.toRadians(l))))
        val eqt = (q / 15.0) - ra

        // Normalize eqt to range [-12, 12]
        var normalizedEqt = eqt
        while (normalizedEqt > 12.0) normalizedEqt -= 24.0
        while (normalizedEqt < -12.0) normalizedEqt += 24.0

        return SolarCoordinates(declination = decl, equationOfTime = normalizedEqt)
    }

    private fun calculateSunHourAngle(lat: Double, decl: Double, angle: Double): Double? {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val angleRad = Math.toRadians(angle)

        val cosH = (sin(angleRad) - sin(latRad) * sin(declRad)) / (cos(latRad) * cos(declRad))
        if (cosH > 1.0 || cosH < -1.0) {
            // Sun never reaches this angle at this latitude (midnight sun or polar night)
            val clamped = cosH.coerceIn(-1.0, 1.0)
            return Math.toDegrees(acos(clamped)) / 15.0
        }
        return Math.toDegrees(acos(cosH)) / 15.0
    }

    private fun calculateAsrAltitude(lat: Double, decl: Double, shadowFactor: Double): Double {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val diff = abs(latRad - declRad)
        val tanDiff = tan(diff)
        val altRad = atan(1.0 / (shadowFactor + tanDiff))
        return Math.toDegrees(altRad)
    }

    private fun hoursToLocalTime(hours: Double, minuteAdjustment: Int): LocalTime {
        var normalized = hours % 24.0
        if (normalized < 0) normalized += 24.0
        val totalSeconds = (normalized * 3600.0).roundToInt() + (minuteAdjustment * 60)
        var clampedSeconds = totalSeconds % 86400
        if (clampedSeconds < 0) clampedSeconds += 86400

        val h = clampedSeconds / 3600
        val m = (clampedSeconds % 3600) / 60
        val s = clampedSeconds % 60
        return LocalTime.of(h.coerceIn(0, 23), m.coerceIn(0, 59), s.coerceIn(0, 59))
    }

    private fun fixAngle(a: Double): Double {
        var angle = a % 360.0
        if (angle < 0) angle += 360.0
        return angle
    }

    private data class HijriDate(val day: Int, val month: Int, val year: Int)

    private fun calculateHijriDate(jd: Double): HijriDate {
        // Kuwaiti / Umm al-Qura standard astronomical approximation algorithm
        val intJd = jd.toInt()
        val l = intJd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val l1 = l - 10631 * n + 354
        val j = (floor((10985 - l1) / 5316.0) * floor((50 * l1) / 17719.0) +
                floor(l1 / 5670.0) * floor((43 * l1) / 15238.0)).toInt()
        val l2 = l1 - floor((30 - j) / 15.0).toInt() * floor((17719 * j) / 50.0).toInt() -
                floor(j / 16.0).toInt() * floor((15238 * j) / 43.0).toInt() + 29
        val m = floor((24 * l2) / 709.0).toInt()
        val d = l2 - floor((709 * m) / 24.0).toInt()
        val y = 30 * n + j - 30

        return HijriDate(
            day = d.coerceIn(1, 30),
            month = m.coerceIn(1, 12),
            year = y.coerceAtLeast(1)
        )
    }
}
