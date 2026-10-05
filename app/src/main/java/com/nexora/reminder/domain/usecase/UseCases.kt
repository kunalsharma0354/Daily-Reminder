package com.nexora.reminder.domain.usecase

import com.nexora.reminder.domain.model.DashboardStats
import com.nexora.reminder.domain.model.HeatmapDay
import com.nexora.reminder.domain.model.Reminder
import com.nexora.reminder.domain.repository.ReminderRepository
import com.nexora.reminder.util.ReminderConstants
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CreateReminder @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(
        taskName: String,
        customTitle: String = "",
        intervalMinutes: Long = 30L,
        voiceText: String = "",
        voicePreviewPath: String = "",
        mode: com.nexora.reminder.domain.model.ReminderMode = com.nexora.reminder.domain.model.ReminderMode.ONCE,
        dailyTimeMinutes: Int = 540
    ): Long {
        validate(taskName, customTitle, intervalMinutes, voiceText, dailyTimeMinutes)
        return repo.create(
            taskName.trim(), customTitle.trim(),
            ReminderConstants.clampInterval(intervalMinutes),
            voiceText.trim(), voicePreviewPath, mode,
            ReminderConstants.clampDailyTime(dailyTimeMinutes)
        )
    }
    private fun validate(name: String, title: String, interval: Long, voice: String, dailyTime: Int) {
        require(name.trim().isNotEmpty()) { "Task name required" }
        require(name.trim().length <= 50) { "Task name max 50 chars" }
        require(title.length <= 40) { "Custom title max 40 chars" }
        require(interval in ReminderConstants.MIN_INTERVAL_MIN..ReminderConstants.MAX_INTERVAL_MIN) {
            "Interval 30 min – 6 hr"
        }
        require(voice.length <= 200) { "Voice text max 200 chars" }
        require(dailyTime in 0..1439) { "Invalid time" }
    }
}

class DeleteReminder @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(id: Long) = repo.delete(id)
}

class CompleteReminder @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(id: Long) = repo.complete(id)
}

class SkipReminder @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(id: Long) = repo.skip(id)
}

class UpdateInterval @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke(id: Long, minutes: Long) = repo.updateInterval(id, minutes)
}

class GetDashboardFlow @Inject constructor(private val repo: ReminderRepository) {
    operator fun invoke(): Flow<List<Reminder>> = repo.observeRemindersToday()
}

class GetStatistics @Inject constructor(private val repo: ReminderRepository) {
    operator fun invoke(): Flow<DashboardStats> = repo.observeStats()
}

class GetHeatmapData @Inject constructor(private val repo: ReminderRepository) {
    operator fun invoke(days: Int = 112): Flow<List<HeatmapDay>> = repo.observeHeatmap(days)
}

class HandleMidnightRollover @Inject constructor(private val repo: ReminderRepository) {
    suspend operator fun invoke() = repo.handleMidnightRollover()
}
