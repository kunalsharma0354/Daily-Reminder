package com.nexora.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY createdAt DESC")
    suspend fun getActive(): List<ReminderEntity>

    @Query("SELECT * FROM reminders ORDER BY createdAt DESC")
    suspend fun getAll(): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ReminderEntity): Long

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Query("UPDATE reminders SET intervalMinutes = :minutes WHERE id = :id")
    suspend fun updateInterval(id: Long, minutes: Long)

    @Query("UPDATE reminders SET voiceText = :text, voiceFilePath = :path WHERE id = :id")
    suspend fun updateVoice(id: Long, text: String, path: String)
}
