package com.nexora.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.core.app.NotificationManagerCompat
import com.nexora.reminder.data.local.CompletionDao
import com.nexora.reminder.data.local.DailyCompletionEntity
import com.nexora.reminder.data.local.ReminderDao
import com.nexora.reminder.domain.model.DayStatus
import com.nexora.reminder.util.DateTimeUtils
import com.nexora.reminder.util.ReminderConstants
import com.nexora.reminder.voice.VoiceStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ReminderAlarmReceiver : BroadcastReceiver() {
    @Inject lateinit var reminderDao: ReminderDao
    @Inject lateinit var completionDao: CompletionDao
    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var helper: NotificationHelper
    @Inject lateinit var voiceStore: VoiceStore

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderConstants.ACTION_NOTIFY) return
        val id = intent.getLongExtra(ReminderConstants.EXTRA_REMINDER_ID, -1L)
        if (id == -1L) return
        helper.ensureChannel()
        CoroutineScope(Dispatchers.IO).launch {
            val entity = reminderDao.getById(id) ?: return@launch
            if (!entity.isActive) return@launch
            val isRepeat = entity.mode == "REPEAT"
            val isDailyTime = entity.mode == "DAILY_TIME"
            val today = DateTimeUtils.todayEpochDay()
            if (!isRepeat) {
                var cur = completionDao.getOne(id, today)
                if (cur == null) {
                    cur = DailyCompletionEntity(id, today, DayStatus.PENDING.name)
                    completionDao.upsert(cur)
                }
                if (cur.status == DayStatus.COMPLETED.name) {
                    // keep next daily alarm alive for DAILY_TIME even after Done
                    if (isDailyTime) {
                        scheduler.scheduleDailyAt(id, ReminderConstants.clampDailyTime(entity.dailyTimeMinutes))
                        try { MidnightScheduler.schedule(context) } catch (_: Exception) { }
                    }
                    return@launch
                }
            }
            val interval = ReminderConstants.clampInterval(entity.intervalMinutes)
            val voicePath = entity.voiceFilePath.takeIf { it.isNotBlank() && File(it).exists() }
                ?: voiceStore.pathFor(id)
            val hasVoice = voicePath != null
            val nm = NotificationManagerCompat.from(context)
            try {
                val notif = NotificationBuilder.build(
                    context, id, entity.taskName, entity.customTitle, interval, hasVoice, isRepeat,
                    if (isDailyTime) ReminderConstants.formatDailyTime(entity.dailyTimeMinutes) else null
                )
                nm.notify(id.toInt(), notif)
            } catch (_: SecurityException) { }
            // Play custom voice message with the notification
            if (hasVoice && voicePath != null) {
                try {
                    val mp = MediaPlayer().apply {
                        setDataSource(voicePath)
                        prepare()
                        start()
                        setOnCompletionListener { try { release() } catch (_: Exception) { } }
                    }
                    // safety release after 30s
                    kotlinx.coroutines.delay(30_000)
                    try { if (mp.isPlaying) mp.stop() } catch (_: Exception) { }
                    try { mp.release() } catch (_: Exception) { }
                } catch (_: Exception) { }
            }
            if (isDailyTime) {
                scheduler.scheduleDailyAt(id, ReminderConstants.clampDailyTime(entity.dailyTimeMinutes))
            } else {
                scheduler.scheduleInMinutes(id, interval)
            }
            try { MidnightScheduler.schedule(context) } catch (_: Exception) { }
        }
    }
}
