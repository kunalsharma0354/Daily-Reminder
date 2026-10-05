package com.nexora.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    @Inject lateinit var scheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIMEZONE_CHANGED,
                Intent.ACTION_DATE_CHANGED
            )
        ) {
            scheduler.rescheduleAll()
            MidnightScheduler.schedule(context)
        }
    }
}
