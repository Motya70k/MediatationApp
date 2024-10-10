package ru.shvetsov.meditationapp.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.shvetsov.meditationapp.data.dao.HistoryDao
import ru.shvetsov.meditationapp.data.entity.History

@Database(entities = [History::class], version = 1)
abstract class HistoryDataBase : RoomDatabase() {
    abstract fun getDao(): HistoryDao
}