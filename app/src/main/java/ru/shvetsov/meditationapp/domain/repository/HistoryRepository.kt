package ru.shvetsov.meditationapp.domain.repository

import ru.shvetsov.meditationapp.data.entity.History

interface HistoryRepository {

    suspend fun getAllHistory(): List<History>

    suspend fun insertHistoryRecord(historyRecord: History)
}