package com.nexora.reminder.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionDao {
    @Query("SELECT * FROM daily_completions WHERE dateEpochDay = :date")
    fun observeForDate(date: Long): Flow<List<DailyCompletionEntity>>

    @Query("SELECT * FROM daily_completions WHERE dateEpochDay = :date")
    suspend fun getForDate(date: Long): List<DailyCompletionEntity>

    @Query("SELECT * FROM daily_completions WHERE reminderId = :reminderId AND dateEpochDay = :date LIMIT 1")
    suspend fun getOne(reminderId: Long, date: Long): DailyCompletionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DailyCompletionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(list: List<DailyCompletionEntity>)

    @Query("DELETE FROM daily_completions WHERE reminderId = :reminderId")
    suspend fun deleteForReminder(reminderId: Long)
}
