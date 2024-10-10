package ru.shvetsov.meditationapp.domain.usecase

import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.domain.repository.HistoryRepository

class HistoryUseCase(
    private val historyRepository: HistoryRepository
) {

    suspend fun getAllHistory(): List<History> {
        return historyRepository.getAllHistory()
    }

    suspend fun insertHistoryRecord(historyRecord: History) {
        historyRepository.insertHistoryRecord(historyRecord)
    }
}