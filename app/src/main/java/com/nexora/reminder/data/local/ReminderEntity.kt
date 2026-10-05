package com.nexora.reminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskName: String,
    val customTitle: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val intervalMinutes: Long = 30L,
    val voiceText: String = "",
    val voiceFilePath: String = "",
    // ONCE = interval until Work Done once a day. REPEAT = rings every interval, no completion.
    // DAILY_TIME = fires once daily at fixed time.
    val mode: String = "ONCE",
    // minutes since midnight for DAILY_TIME (0..1439), default 09:00
    val dailyTimeMinutes: Int = 540
)
