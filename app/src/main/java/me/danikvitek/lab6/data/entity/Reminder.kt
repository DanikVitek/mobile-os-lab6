package me.danikvitek.lab6.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import me.danikvitek.lab6.util.serializer.DateSerializer
import java.util.Date

@Serializable
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long,

    var title: String,

    @ColumnInfo(defaultValue = "")
    var text: String,

    @Serializable(with = DateSerializer::class)
    var datetime: Date,
)