package ru.shvetsov.meditationapp.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class History(
    @PrimaryKey (autoGenerate = true) val id: Int?,
    @ColumnInfo (name = "date") val date: String,
    @ColumnInfo (name = "meditation_time") val meditationTime: Long
)
