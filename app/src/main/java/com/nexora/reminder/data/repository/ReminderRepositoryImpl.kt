package com.nexora.reminder.data.repository

import com.nexora.reminder.data.local.CompletionDao
import com.nexora.reminder.data.local.DailyCompletionEntity
import com.nexora.reminder.data.local.HistoryDao
import com.nexora.reminder.data.local.HistoryEntity
import com.nexora.reminder.data.local.ReminderDao
import com.nexora.reminder.data.local.ReminderEntity
import com.nexora.reminder.domain.model.DashboardStats
import com.nexora.reminder.domain.model.DayStatus
import com.nexora.reminder.domain.model.HeatState
import com.nexora.reminder.domain.model.HeatmapDay
import com.nexora.reminder.domain.model.Reminder
import com.nexora.reminder.domain.model.ReminderMode
import com.nexora.reminder.domain.repository.ReminderRepository
import com.nexora.reminder.notification.NotificationScheduler
import com.nexora.reminder.util.DateTimeUtils
import com.nexora.reminder.util.ReminderConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao,
    private val completionDao: CompletionDao,
    private val historyDao: HistoryDao,
    private val scheduler: NotificationScheduler,
    private val voiceStore: com.nexora.reminder.voice.VoiceStore
) : ReminderRepository {

    override fun observeRemindersToday(): Flow<List<Reminder>> {
        val today = DateTimeUtils.todayEpochDay()
        return combine(
            reminderDao.observeActive(),
            completionDao.observeForDate(today)
        ) { reminders, completions ->
            val map = completions.associateBy { it.reminderId }
            reminders.map { e ->
                val st = map[e.id]?.status?.let { runCatching { DayStatus.valueOf(it) }.getOrDefault(DayStatus.PENDING) }
                    ?: DayStatus.PENDING
                val mode = runCatching { ReminderMode.valueOf(e.mode) }.getOrDefault(ReminderMode.ONCE)
                Reminder(
                    e.id, e.taskName, e.customTitle, e.createdAt, e.isActive, st,
                    ReminderConstants.clampInterval(e.intervalMinutes),
                    e.voiceText, e.voiceFilePath, mode,
                    ReminderConstants.clampDailyTime(e.dailyTimeMinutes)
                )
            }
        }
    }

    override suspend fun create(
        taskName: String,
        customTitle: String,
        intervalMinutes: Long,
        voiceText: String,
        voicePreviewPath: String,
        mode: ReminderMode,
        dailyTimeMinutes: Int
    ): Long {
        val name = taskName.trim().take(50)
        require(name.isNotEmpty()) { "Task name required" }
        val interval = ReminderConstants.clampInterval(intervalMinutes)
        val vText = voiceText.trim().take(200)
        val time = ReminderConstants.clampDailyTime(dailyTimeMinutes)
        val id = reminderDao.upsert(
            ReminderEntity(
                taskName = name,
                customTitle = customTitle.trim().take(40),
                intervalMinutes = interval,
                voiceText = vText,
                mode = mode.name,
                dailyTimeMinutes = time
            )
        )
        // attach voice file if preview exists (auto-saved at convert time)
        if (vText.isNotBlank() && voicePreviewPath.isNotBlank()) {
            val finalPath = voiceStore.saveFromPreview(id, voicePreviewPath) ?: ""
            if (finalPath.isNotBlank()) {
                reminderDao.updateVoice(id, vText, finalPath)
            }
        }
        val today = DateTimeUtils.todayEpochDay()
        completionDao.upsert(DailyCompletionEntity(id, today, DayStatus.PENDING.name))
        if (mode == ReminderMode.DAILY_TIME) {
            scheduler.scheduleDailyAt(id, ReminderConstants.clampDailyTime(dailyTimeMinutes))
        } else {
            scheduler.scheduleFirst(id)
        }
        return id
    }

    override suspend fun delete(id: Long) {
        scheduler.cancel(id)
        try { voiceStore.delete(id) } catch (_: Exception) { }
        completionDao.deleteForReminder(id)
        historyDao.deleteForReminder(id)
        reminderDao.deleteById(id)
    }

    override suspend fun complete(id: Long) {
        // REPEAT tasks never complete — no Work Done, no history
        val entity = reminderDao.getById(id)
        if (entity?.mode == ReminderMode.REPEAT.name) {
            scheduler.scheduleNext(id)
            return
        }
        val today = DateTimeUtils.todayEpochDay()
        completionDao.upsert(
            DailyCompletionEntity(id, today, DayStatus.COMPLETED.name, System.currentTimeMillis())
        )
        scheduler.cancel(id)
        historyDao.insert(HistoryEntity(reminderId = id, dateEpochDay = today, status = DayStatus.COMPLETED.name, completedAt = System.currentTimeMillis()))
    }

    override suspend fun skip(id: Long) {
        scheduler.scheduleNext(id)
    }

    override suspend fun updateInterval(id: Long, minutes: Long) {
        val clamped = ReminderConstants.clampInterval(minutes)
        reminderDao.updateInterval(id, clamped)
        val entity = reminderDao.getById(id)
        if (entity?.mode == "DAILY_TIME") return // fixed time uses no interval reschedule
        // reschedule with new interval (keep cycle alive if still pending today)
        val today = DateTimeUtils.todayEpochDay()
        val cur = completionDao.getOne(id, today)
        if (cur?.status != DayStatus.COMPLETED.name) {
            scheduler.cancel(id)
            scheduler.scheduleInMinutes(id, clamped)
        }
    }

    override suspend fun getActiveIds(): List<Long> = reminderDao.getActive().map { it.id }

    override suspend fun handleMidnightRollover() {
        val today = DateTimeUtils.todayEpochDay()
        val yesterday = today - 1
        val active = reminderDao.getActive()
        if (active.isEmpty()) return
        // REPEAT tasks: no history, no completion tracking — just keep ringing 24/7
        // ONCE + DAILY_TIME count in stats/history.
        val tracked = active.filter { it.mode != ReminderMode.REPEAT.name }
        val repeat = active.filter { it.mode == ReminderMode.REPEAT.name }
        if (tracked.isNotEmpty()) {
            val yCompletions = completionDao.getForDate(yesterday)
            val yMap = yCompletions.associateBy { it.reminderId }
            val toHistory = mutableListOf<HistoryEntity>()
            val toToday = mutableListOf<DailyCompletionEntity>()
            for (r in tracked) {
                val y = yMap[r.id]
                val status = if (y?.status == DayStatus.COMPLETED.name) DayStatus.COMPLETED else DayStatus.MISSED
                toHistory.add(HistoryEntity(reminderId = r.id, dateEpochDay = yesterday, status = status.name))
                toToday.add(DailyCompletionEntity(r.id, today, DayStatus.PENDING.name))
            }
            val existing = historyDao.getAll().filter { it.dateEpochDay == yesterday }.map { it.reminderId to it.status }.toSet()
            val filtered = toHistory.filterNot { (it.reminderId to DayStatus.COMPLETED.name) in existing && it.status == DayStatus.MISSED.name }
            historyDao.insertAll(filtered)
            completionDao.upsertAll(toToday)
            for (r in tracked) {
                if (r.mode == ReminderMode.DAILY_TIME.name) {
                    scheduler.scheduleDailyAt(r.id, ReminderConstants.clampDailyTime(r.dailyTimeMinutes))
                } else {
                    scheduler.scheduleFirst(r.id)
                }
            }
        }
        for (r in repeat) scheduler.scheduleFirst(r.id)
    }

    override fun observeStats(): Flow<DashboardStats> {
        return historyDao.observeAll().map { all ->
            val byDate = all.groupBy { it.dateEpochDay }
            var completed = 0
            var missed = 0
            for ((_, entries) in byDate) {
                if (entries.any { it.status == DayStatus.COMPLETED.name }) completed++ else missed++
            }
            val completedDates = byDate.filter { (_, v) -> v.any { it.status == DayStatus.COMPLETED.name } }.keys.sortedDescending()
            var streak = 0
            var cursor = DateTimeUtils.todayEpochDay()
            if (!completedDates.contains(cursor)) cursor -= 1
            while (completedDates.contains(cursor)) {
                streak++
                cursor -= 1
            }
            DashboardStats(completed, missed, streak)
        }
    }

    override fun observeHeatmap(days: Int): Flow<List<HeatmapDay>> {
        return combine(
            historyDao.observeAll(),
            completionDao.observeForDate(DateTimeUtils.todayEpochDay()),
            reminderDao.observeActive()
        ) { history, todayCompletions, active ->
            val today = DateTimeUtils.todayEpochDay()
            val start = today - (days - 1)
            val byDateStatus = history.groupBy { it.dateEpochDay }.mapValues { (_, v) ->
                if (v.any { it.status == DayStatus.COMPLETED.name }) HeatState.COMPLETED else HeatState.MISSED
            }.toMutableMap()
            val todayMap = todayCompletions.associateBy { it.reminderId }
            val anyTodayCompleted = todayMap.values.any { it.status == DayStatus.COMPLETED.name }
            val hasActive = active.isNotEmpty()
            (0 until days).map { offset ->
                val d = start + offset
                val isToday = d == today
                val state = when {
                    isToday && anyTodayCompleted -> HeatState.TODAY_COMPLETED
                    isToday && hasActive -> HeatState.TODAY_PENDING
                    isToday -> HeatState.INACTIVE
                    byDateStatus.containsKey(d) -> byDateStatus[d]!!
                    else -> HeatState.INACTIVE
                }
                HeatmapDay(d, state, isToday)
            }
        }
    }
}
