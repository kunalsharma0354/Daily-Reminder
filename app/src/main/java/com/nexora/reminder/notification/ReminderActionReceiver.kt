package com.nexora.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.nexora.reminder.data.local.CompletionDao
import com.nexora.reminder.data.local.DailyCompletionEntity
import com.nexora.reminder.data.local.HistoryDao
import com.nexora.reminder.data.local.HistoryEntity
import com.nexora.reminder.data.local.ReminderDao
import com.nexora.reminder.domain.model.DayStatus
import com.nexora.reminder.util.DateTimeUtils
import com.nexora.reminder.util.ReminderConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ReminderActionReceiver : BroadcastReceiver() {
    @Inject lateinit var completionDao: CompletionDao
    @Inject lateinit var historyDao: HistoryDao
    @Inject lateinit var reminderDao: ReminderDao
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderConstants.EXTRA_REMINDER_ID, -1L)
        if (id == -1L) return
        when (intent.action) {
            ReminderConstants.ACTION_DONE -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val today = DateTimeUtils.todayEpochDay()
                    completionDao.upsert(
                        DailyCompletionEntity(id, today, DayStatus.COMPLETED.name, System.currentTimeMillis())
                    )
                    historyDao.insert(
                        HistoryEntity(reminderId = id, dateEpochDay = today, status = DayStatus.COMPLETED.name, completedAt = System.currentTimeMillis())
                    )
                    // Stop only for TODAY — midnight restarts automatically (24/7)
                    scheduler.cancel(id)
                    try {
                        NotificationManagerCompat.from(context).cancel(id.toInt())
                    } catch (_: Exception) { }
                    try { MidnightScheduler.schedule(context) } catch (_: Exception) { }
                }
            }
            ReminderConstants.ACTION_SKIP -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val e = reminderDao.getById(id)
                    // Daily fixed-time: snooze 30 min (next daily alarm re-arms on fire).
                    val mins = if (e?.mode == "DAILY_TIME") 30L
                    else ReminderConstants.clampInterval(e?.intervalMinutes ?: ReminderConstants.REPEAT_INTERVAL_MIN)
                    scheduler.scheduleInMinutes(id, mins)
                    try {
                        NotificationManagerCompat.from(context).cancel(id.toInt())
                    } catch (_: Exception) { }
                }
            }
        }
    }
}
