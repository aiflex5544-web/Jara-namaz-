package com.example

import com.example.data.calculator.PrayerTimeCalculator
import com.example.data.model.CalculationMethod
import com.example.data.model.CityDirectory
import com.example.data.model.JuristicMethod
import com.example.data.model.PrayerType
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class ExampleUnitTest {

    @Test
    fun testPrayerTimesChronologicalOrder() {
        val testDate = LocalDate.of(2026, 10, 15)
        val makkah = CityDirectory.DEFAULT_CITY

        val schedule = PrayerTimeCalculator.calculate(
            date = testDate,
            latitude = makkah.latitude,
            longitude = makkah.longitude,
            timeZoneId = makkah.timeZoneId,
            method = CalculationMethod.UMM_AL_QURA,
            juristic = JuristicMethod.STANDARD
        )

        assertNotNull(schedule.fajr)
        assertNotNull(schedule.sunrise)
        assertNotNull(schedule.dhuhr)
        assertNotNull(schedule.asr)
        assertNotNull(schedule.maghrib)
        assertNotNull(schedule.isha)

        // Verify logical progression of daily prayers
        assertTrue("Fajr must be before Sunrise", schedule.fajr.isBefore(schedule.sunrise))
        assertTrue("Sunrise must be before Dhuhr", schedule.sunrise.isBefore(schedule.dhuhr))
        assertTrue("Dhuhr must be before Asr", schedule.dhuhr.isBefore(schedule.asr))
        assertTrue("Asr must be before Maghrib", schedule.asr.isBefore(schedule.maghrib))
        assertTrue("Maghrib must be before Isha", schedule.maghrib.isBefore(schedule.isha))
    }

    @Test
    fun testHanafiAsrIsLaterThanStandard() {
        val testDate = LocalDate.of(2026, 10, 15)
        val karachi = CityDirectory.findCityById("pk_khi")!!

        val hanafiSchedule = PrayerTimeCalculator.calculate(
            date = testDate,
            latitude = karachi.latitude,
            longitude = karachi.longitude,
            timeZoneId = karachi.timeZoneId,
            method = CalculationMethod.KARACHI,
            juristic = JuristicMethod.HANAFI
        )

        val standardSchedule = PrayerTimeCalculator.calculate(
            date = testDate,
            latitude = karachi.latitude,
            longitude = karachi.longitude,
            timeZoneId = karachi.timeZoneId,
            method = CalculationMethod.KARACHI,
            juristic = JuristicMethod.STANDARD
        )

        assertTrue(
            "Hanafi Asr (${hanafiSchedule.asr}) must be after Standard Asr (${standardSchedule.asr})",
            hanafiSchedule.asr.isAfter(standardSchedule.asr)
        )
    }

    @Test
    fun testHijriDateCalculation() {
        val testDate = LocalDate.of(2026, 10, 15)
        val schedule = PrayerTimeCalculator.calculate(
            date = testDate,
            latitude = 21.4225,
            longitude = 39.8262,
            timeZoneId = "Asia/Riyadh",
            method = CalculationMethod.UMM_AL_QURA,
            juristic = JuristicMethod.STANDARD
        )

        assertTrue(schedule.hijriDay in 1..30)
        assertTrue(schedule.hijriYear in 1445..1455)
        assertTrue(schedule.hijriMonthName.isNotBlank())
        assertTrue(schedule.hijriMonthNameUrdu.isNotBlank())
    }
}
