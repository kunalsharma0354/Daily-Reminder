package com.nexora.reminder.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexora.reminder.data.local.AppDatabase
import com.nexora.reminder.data.repository.ReminderRepositoryImpl
import com.nexora.reminder.domain.usecase.HandleMidnightRollover
import com.nexora.reminder.util.ReminderConstants
import androidx.room.Room

class MidnightWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return try {
            val db = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                ReminderConstants.DATABASE_NAME
            ).fallbackToDestructiveMigration().build()
            val helper = NotificationHelper(applicationContext)
            val scheduler = NotificationScheduler(applicationContext, db.reminderDao(), helper)
            val voiceStore = com.nexora.reminder.voice.VoiceStore(applicationContext)
            val repo = ReminderRepositoryImpl(db.reminderDao(), db.completionDao(), db.historyDao(), scheduler, voiceStore)
            HandleMidnightRollover(repo)()
            MidnightScheduler.schedule(applicationContext)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
