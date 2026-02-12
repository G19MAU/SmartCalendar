package se.umu.calu0217.smartcalendar.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String?,
    val location: String?,
    val dueDate: String,
    val completed: Boolean,
    val category: String?,
    val updatedAt: String?,
    val dirty: Boolean = false,
    val recurrence: String
)

