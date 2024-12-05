package me.danikvitek.lab6.data.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.danikvitek.lab6.data.entity.Reminder
import java.util.Date

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY datetime DESC")
    fun getAll(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    fun getById(id: Long): Flow<Reminder?>

    @Query("INSERT INTO reminders (title, text, datetime) VALUES (:title, :text, :datetime)")
    suspend fun insert(title: String, text: String = "", datetime: Date)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reminders ORDER BY id DESC LIMIT 1")
    suspend fun getLastAdded(): Reminder?
}