package se.umu.calu0217.smartcalendar.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String?,
    val startDate: String,
    val endDate: String,
    val category: String?,
    val recurrence: String
)

