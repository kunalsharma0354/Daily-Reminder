package com.nexora.reminder.util

object ReminderConstants {
    const val DATABASE_NAME = "daily_reminder.db"
    const val CHANNEL_ID = "daily_reminder_channel"
    const val CHANNEL_NAME = "Daily Reminders"
    const val MIDNIGHT_WORK = "midnight_rollover"
    fun schedulerWork(reminderId: Long) = "notification_scheduler_$reminderId"

    const val FIRST_REMINDER_DELAY_MIN = 1L
    const val REPEAT_INTERVAL_MIN = 30L

    // User-configurable interval: 30 min .. 6 hr
    const val MIN_INTERVAL_MIN = 30L
    const val MAX_INTERVAL_MIN = 360L
    val PRESETS_MIN = listOf(30L, 60L, 90L, 120L, 180L, 360L)

    const val ACTION_DONE = "com.nexora.reminder.ACTION_DONE"
    const val ACTION_SKIP = "com.nexora.reminder.ACTION_SKIP"
    const val ACTION_NOTIFY = "com.nexora.reminder.ACTION_NOTIFY"

    const val EXTRA_REMINDER_ID = "reminder_id"

    const val HEATMAP_DAYS = 112 // ~16 weeks

    fun formatInterval(min: Long): String = when {
        min < 60 -> "${min}m"
        min % 60 == 0L -> "${min / 60}h"
        else -> "${min / 60}h ${min % 60}m"
    }

    fun clampInterval(min: Long): Long = min.coerceIn(MIN_INTERVAL_MIN, MAX_INTERVAL_MIN)

    const val DEFAULT_DAILY_TIME_MIN = 540 // 09:00 AM
    fun clampDailyTime(min: Int): Int = min.coerceIn(0, 1439)

    fun formatDailyTime(minutesOfDay: Int): String {
        val m = clampDailyTime(minutesOfDay)
        val h24 = m / 60
        val mm = m % 60
        val amPm = if (h24 < 12) "AM" else "PM"
        val h12 = when (h24 % 12) { 0 -> 12 else -> h24 % 12 }
        return "%02d:%02d %s".format(h12, mm, amPm)
    }
}
