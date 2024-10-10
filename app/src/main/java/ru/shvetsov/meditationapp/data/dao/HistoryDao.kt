package ru.shvetsov.meditationapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.shvetsov.meditationapp.data.entity.History

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history")
    suspend fun getAll(): List<History>

    @Insert
    suspend fun insertHistoryRecord(historyRecord: History)
}