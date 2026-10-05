package com.nexora.reminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.nexora.reminder.data.local.ReminderDao
import com.nexora.reminder.util.ReminderConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reminderDao: ReminderDao,
    private val helper: NotificationHelper
) {
    private fun alarmManager(): AlarmManager? =
        context.getSystemService(AlarmManager::class.java)

    private fun pendingFor(reminderId: Long, action: String, reqOffset: Int): PendingIntent {
        val i = Intent(context, ReminderAlarmReceiver::class.java).apply {
            this.action = action
            putExtra(ReminderConstants.EXTRA_REMINDER_ID, reminderId)
        }
        val req = (reminderId * 10 + reqOffset).toInt()
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, req, i, flags)
    }

    fun scheduleFirst(reminderId: Long) {
        scheduleInMinutes(reminderId, ReminderConstants.FIRST_REMINDER_DELAY_MIN)
    }

    suspend fun intervalFor(reminderId: Long): Long {
        val e = reminderDao.getById(reminderId)
        return ReminderConstants.clampInterval(e?.intervalMinutes ?: ReminderConstants.REPEAT_INTERVAL_MIN)
    }

    fun scheduleNext(reminderId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val e = reminderDao.getById(reminderId)
            if (e?.mode == "DAILY_TIME") {
                scheduleDailyAt(reminderId, ReminderConstants.clampDailyTime(e.dailyTimeMinutes))
            } else {
                val mins = ReminderConstants.clampInterval(e?.intervalMinutes ?: ReminderConstants.REPEAT_INTERVAL_MIN)
                scheduleInMinutes(reminderId, mins)
            }
        }
    }

    /** Schedule the next daily occurrence at [timeMinutes] (minutes since midnight). */
    fun scheduleDailyAt(reminderId: Long, timeMinutes: Int) {
        helper.ensureChannel()
        val triggerAt = nextDailyTrigger(timeMinutes)
        setExact(triggerAt, pendingFor(reminderId, ReminderConstants.ACTION_NOTIFY, 1))
    }

    fun nextDailyTrigger(timeMinutes: Int): Long {
        val t = ReminderConstants.clampDailyTime(timeMinutes)
        val now = Calendar.getInstance()
        val target = (Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, t / 60)
            set(Calendar.MINUTE, t % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        })
        if (target.timeInMillis <= now.timeInMillis + 30_000L) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis
    }

    private fun setExact(triggerAt: Long, pi: PendingIntent) {
        val am = alarmManager()
        val canExact = if (Build.VERSION.SDK_INT >= 31) {
            am?.canScheduleExactAlarms() == true
        } else true
        try {
            if (canExact) {
                if (Build.VERSION.SDK_INT >= 23) {
                    am?.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } else {
                    @Suppress("DEPRECATION")
                    am?.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                }
            } else {
                am?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: SecurityException) {
            am?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun scheduleInMinutes(reminderId: Long, minutes: Long) {
        helper.ensureChannel()
        val triggerAt = System.currentTimeMillis() + minutes * 60_000L
        setExact(triggerAt, pendingFor(reminderId, ReminderConstants.ACTION_NOTIFY, 1))
    }

    fun cancel(reminderId: Long) {
        val am = alarmManager()
        for (offset in 1..3) {
            try {
                val pi = pendingFor(reminderId, ReminderConstants.ACTION_NOTIFY, offset)
                am?.cancel(pi)
                pi.cancel()
            } catch (_: Exception) { }
        }
        try {
            val nm = androidx.core.app.NotificationManagerCompat.from(context)
            nm.cancel(reminderId.toInt())
        } catch (_: Exception) { }
    }

    fun rescheduleAll() {
        CoroutineScope(Dispatchers.IO).launch {
            val active = reminderDao.getActive()
            for (r in active) {
                if (r.mode == "DAILY_TIME") {
                    scheduleDailyAt(r.id, ReminderConstants.clampDailyTime(r.dailyTimeMinutes))
                } else {
                    scheduleFirst(r.id)
                }
            }
        }
    }
}
