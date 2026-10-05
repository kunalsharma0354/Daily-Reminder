package com.nexora.reminder

import android.app.Application
import com.nexora.reminder.notification.MidnightScheduler
import com.nexora.reminder.notification.NotificationHelper
import com.nexora.reminder.notification.NotificationScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DailyReminderApplication : Application() {
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()
        notificationHelper.ensureChannel()
        // 24/7 guarantee: every process start, restore all cycles + midnight watchdog
        MidnightScheduler.schedule(this)
        CoroutineScope(Dispatchers.IO).launch {
            try { scheduler.rescheduleAll() } catch (_: Exception) { }
        }
    }
}
