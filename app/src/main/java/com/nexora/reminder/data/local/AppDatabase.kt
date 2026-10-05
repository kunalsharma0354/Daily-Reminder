package com.nexora.reminder.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ReminderEntity::class, DailyCompletionEntity::class, HistoryEntity::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun completionDao(): CompletionDao
    abstract fun historyDao(): HistoryDao
}
