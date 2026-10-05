package com.nexora.reminder.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.nexora.reminder.data.local.AppDatabase
import com.nexora.reminder.data.local.CompletionDao
import com.nexora.reminder.data.local.HistoryDao
import com.nexora.reminder.data.local.ReminderDao
import com.nexora.reminder.data.repository.ReminderRepositoryImpl
import com.nexora.reminder.domain.repository.ReminderRepository
import com.nexora.reminder.util.ReminderConstants
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDb(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, ReminderConstants.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideReminderDao(db: AppDatabase): ReminderDao = db.reminderDao()
    @Provides fun provideCompletionDao(db: AppDatabase): CompletionDao = db.completionDao()
    @Provides fun provideHistoryDao(db: AppDatabase): HistoryDao = db.historyDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindRepo(impl: ReminderRepositoryImpl): ReminderRepository
}
