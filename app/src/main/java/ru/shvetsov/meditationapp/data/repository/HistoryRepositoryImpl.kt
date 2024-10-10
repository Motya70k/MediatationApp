package ru.shvetsov.meditationapp.data.repository

import android.util.Log
import ru.shvetsov.meditationapp.data.dao.HistoryDao
import ru.shvetsov.meditationapp.data.entity.History
import ru.shvetsov.meditationapp.domain.repository.HistoryRepository

class HistoryRepositoryImpl (private val historyDao: HistoryDao) :
    HistoryRepository {

    override suspend fun getAllHistory(): List<History> {
        return historyDao.getAll()
    }

    override suspend fun insertHistoryRecord(historyRecord: History) {
        Log.d("HistoryRepo", "Inserting record: $historyRecord")
        historyDao.insertHistoryRecord(historyRecord)
    }
}