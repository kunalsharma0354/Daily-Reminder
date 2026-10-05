package com.nexora.reminder.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.nexora.reminder.MainActivity
import com.nexora.reminder.util.ReminderConstants

object NotificationBuilder {
    fun build(
        context: Context,
        reminderId: Long,
        taskName: String,
        customTitle: String,
        intervalMinutes: Long = 30L,
        hasCustomVoice: Boolean = false,
        isRepeat: Boolean = false,
        dailyTimeLabel: String? = null
    ): android.app.Notification {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderConstants.EXTRA_REMINDER_ID, reminderId)
        }
        val openPi = PendingIntent.getActivity(
            context, (reminderId * 10 + 9).toInt(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderConstants.ACTION_DONE
            putExtra(ReminderConstants.EXTRA_REMINDER_ID, reminderId)
        }
        val donePi = PendingIntent.getBroadcast(
            context, (reminderId * 10 + 2).toInt(), doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val skipIntent = Intent(context, ReminderActionReceiver::class.java).apply {
            action = ReminderConstants.ACTION_SKIP
            putExtra(ReminderConstants.EXTRA_REMINDER_ID, reminderId)
        }
        val skipPi = PendingIntent.getBroadcast(
            context, (reminderId * 10 + 3).toInt(), skipIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val every = ReminderConstants.formatInterval(ReminderConstants.clampInterval(intervalMinutes))
        val skipLabel = if (dailyTimeLabel != null) "Snooze 30m" else "Skip $every"
        val title = if (customTitle.isNotBlank()) customTitle else "Time for: $taskName"
        val text = when {
            dailyTimeLabel != null -> "$taskName — daily at $dailyTimeLabel • Work Done or $skipLabel"
            isRepeat -> "$taskName — every $every"
            else -> "$taskName — every $every • Work Done or $skipLabel"
        }

        val builder = NotificationCompat.Builder(context, ReminderConstants.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setOngoing(false)
            .setContentIntent(openPi)
        // Repeat tasks need no completion — only skip/dismiss.
        if (!isRepeat) builder.addAction(android.R.drawable.checkbox_on_background, "Work Done", donePi)
        builder.addAction(android.R.drawable.ic_media_pause, skipLabel, skipPi)
        // Custom voice plays via MediaPlayer; keep system sound off to avoid overlap.
        if (hasCustomVoice) builder.setSilent(true)
        return builder.build()
    }
}
