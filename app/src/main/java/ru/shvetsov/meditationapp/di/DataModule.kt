package ru.shvetsov.meditationapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.shvetsov.meditationapp.data.dao.HistoryDao
import ru.shvetsov.meditationapp.data.db.HistoryDataBase
import ru.shvetsov.meditationapp.data.repository.HistoryRepositoryImpl
import ru.shvetsov.meditationapp.domain.repository.HistoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    @Provides
    @Singleton
    fun provideHistoryRepository(historyDao: HistoryDao): HistoryRepository {
        return HistoryRepositoryImpl(historyDao)
    }

    @Provides
    @Singleton
    fun provideDao(historyDataBase: HistoryDataBase): HistoryDao {
        return historyDataBase.getDao()
    }

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): HistoryDataBase {
        return Room.databaseBuilder(
            context,
            HistoryDataBase::class.java,
            "history"
        ).build()
    }
}