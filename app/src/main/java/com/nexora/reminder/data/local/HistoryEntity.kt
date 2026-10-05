package com.nexora.reminder.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [Index("dateEpochDay"), Index(value = ["reminderId", "dateEpochDay"])]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reminderId: Long,
    val dateEpochDay: Long,
    val status: String,
    val completedAt: Long? = null
)
