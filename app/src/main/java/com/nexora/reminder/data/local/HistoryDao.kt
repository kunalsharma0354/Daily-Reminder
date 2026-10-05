package com.nexora.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history ORDER BY dateEpochDay DESC")
    fun observeAll(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history ORDER BY dateEpochDay DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<HistoryEntity>

    @Query("SELECT * FROM history")
    suspend fun getAll(): List<HistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<HistoryEntity>)

    @Query("SELECT DISTINCT dateEpochDay FROM history WHERE status = 'COMPLETED'")
    suspend fun completedDates(): List<Long>

    @Query("DELETE FROM history WHERE reminderId = :reminderId")
    suspend fun deleteForReminder(reminderId: Long)
}
