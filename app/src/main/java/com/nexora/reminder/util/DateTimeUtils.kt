package com.nexora.reminder.util

import java.time.LocalDate
import java.time.ZoneId

object DateTimeUtils {
    fun today(): LocalDate = LocalDate.now(ZoneId.systemDefault())

    fun todayEpochDay(): Long = today().toEpochDay()

    fun fromEpochDay(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

    fun isToday(epochDay: Long): Boolean = epochDay == todayEpochDay()
}
