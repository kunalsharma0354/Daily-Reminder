package com.nexora.reminder.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MidnightReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val req = OneTimeWorkRequestBuilder<MidnightWorker>().build()
            WorkManager.getInstance(context).enqueue(req)
        } catch (_: Exception) { }
        // schedule next
        MidnightScheduler.schedule(context)
    }
}
