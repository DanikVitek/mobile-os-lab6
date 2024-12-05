package me.danikvitek.lab6.data

import androidx.room.TypeConverter
import java.util.Date

object Converters {
    @TypeConverter
    fun dateToLong(timestamp: Date): Long = timestamp.time

    @TypeConverter
    fun longToDate(long: Long): Date = Date(long)
}