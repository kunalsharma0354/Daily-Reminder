package com.nexora.reminder.data.local

import androidx.room.Entity

@Entity(tableName = "daily_completions", primaryKeys = ["reminderId", "dateEpochDay"])
data class DailyCompletionEntity(
    val reminderId: Long,
    val dateEpochDay: Long,
    val status: String, // PENDING, COMPLETED, MISSED
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)
