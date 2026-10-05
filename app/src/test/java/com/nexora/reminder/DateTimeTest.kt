package com.nexora.reminder

import com.nexora.reminder.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateTimeTest {
    @Test
    fun today_matches_system() {
        val expected = LocalDate.now().toEpochDay()
        assertEquals(expected, DateTimeUtils.todayEpochDay())
    }

    @Test
    fun roundtrip_epochDay() {
        val today = DateTimeUtils.todayEpochDay()
        assertEquals(today, DateTimeUtils.fromEpochDay(today).toEpochDay())
        assertTrue(DateTimeUtils.isToday(today))
    }
}
