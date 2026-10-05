package com.nexora.reminder.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nexora.reminder.R
import com.nexora.reminder.util.ReminderConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            if (nm?.getNotificationChannel(ReminderConstants.CHANNEL_ID) == null) {
                val ch = NotificationChannel(
                    ReminderConstants.CHANNEL_ID,
                    ReminderConstants.CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Persistent daily task reminders"
                    enableVibration(true)
                    vibrationPattern = longArrayOf(0, 250, 250, 250)
                }
                nm?.createNotificationChannel(ch)
            }
        }
    }
}
