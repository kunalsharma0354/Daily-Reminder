package com.nexora.reminder.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.nexora.reminder.util.ReminderConstants
import java.util.Calendar
import java.util.concurrent.TimeUnit

object MidnightScheduler {
    fun schedule(context: Context) {
        try {
            val req = PeriodicWorkRequestBuilder<MidnightWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(delayToMidnight(), TimeUnit.MILLISECONDS)
                .addTag(ReminderConstants.MIDNIGHT_WORK)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                ReminderConstants.MIDNIGHT_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                req
            )
        } catch (_: Exception) { }

        // Exact midnight alarm — guarantees restart even if WorkManager delayed by Doze.
        // This NEVER stops reminders: rollover only resets day-status + history, then restarts cycle in ~1 min.
        try {
            val am = context.getSystemService(AlarmManager::class.java) ?: return
            val intent = Intent(context, MidnightReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, 999001, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val triggerAt = System.currentTimeMillis() + delayToMidnight() + 5_000L
            if (Build.VERSION.SDK_INT >= 31 && am.canScheduleExactAlarms()) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        } catch (_: Exception) { }
    }

    private fun delayToMidnight(): Long {
        val now = Calendar.getInstance()
        val midnight = (Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 5)
            set(Calendar.MILLISECOND, 0)
        })
        return (midnight.timeInMillis - now.timeInMillis).coerceAtLeast(60_000L)
    }
}
