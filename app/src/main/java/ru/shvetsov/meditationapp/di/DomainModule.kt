package ru.shvetsov.meditationapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import ru.shvetsov.meditationapp.domain.repository.HistoryRepository
import ru.shvetsov.meditationapp.domain.usecase.HistoryUseCase

@Module
@InstallIn(ViewModelComponent::class)
class DomainModule {

    @Provides
    fun provideHistoryUseCase(historyRepository: HistoryRepository): HistoryUseCase {
        return HistoryUseCase(historyRepository)
    }
}