package com.nexora.reminder.domain.repository

import com.nexora.reminder.domain.model.DashboardStats
import com.nexora.reminder.domain.model.HeatmapDay
import com.nexora.reminder.domain.model.Reminder
import com.nexora.reminder.domain.model.ReminderMode
import kotlinx.coroutines.flow.Flow

interface ReminderRepository {
    fun observeRemindersToday(): Flow<List<Reminder>>
    suspend fun create(
        taskName: String,
        customTitle: String,
        intervalMinutes: Long = 30L,
        voiceText: String = "",
        voicePreviewPath: String = "",
        mode: ReminderMode = ReminderMode.ONCE,
        dailyTimeMinutes: Int = 540
    ): Long
    suspend fun delete(id: Long)
    suspend fun complete(id: Long)
    suspend fun skip(id: Long)
    suspend fun updateInterval(id: Long, minutes: Long)
    suspend fun getActiveIds(): List<Long>
    suspend fun handleMidnightRollover()
    fun observeStats(): Flow<DashboardStats>
    fun observeHeatmap(days: Int = 112): Flow<List<HeatmapDay>>
}
