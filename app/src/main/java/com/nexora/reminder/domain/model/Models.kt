package com.nexora.reminder.domain.model

enum class DayStatus { PENDING, COMPLETED, MISSED }

enum class ReminderMode { ONCE, REPEAT, DAILY_TIME }

data class Reminder(
    val id: Long = 0,
    val taskName: String,
    val customTitle: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val todayStatus: DayStatus = DayStatus.PENDING,
    val intervalMinutes: Long = 30L,
    val voiceText: String = "",
    val voiceFilePath: String = "",
    val mode: ReminderMode = ReminderMode.ONCE,
    val dailyTimeMinutes: Int = 540
) {
    val hasVoice: Boolean get() = voiceText.isNotBlank() && voiceFilePath.isNotBlank()
    val isRepeat: Boolean get() = mode == ReminderMode.REPEAT
    val isDailyTime: Boolean get() = mode == ReminderMode.DAILY_TIME
}

data class DashboardStats(
    val completedDays: Int = 0,
    val missedDays: Int = 0,
    val currentStreak: Int = 0
)

enum class HeatState { COMPLETED, MISSED, INACTIVE, TODAY_PENDING, TODAY_COMPLETED }

data class HeatmapDay(
    val epochDay: Long,
    val state: HeatState,
    val isToday: Boolean = false
)
