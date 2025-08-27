package se.umu.calu0217.smartcalendar.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ActivityEntity::class, TaskEntity::class, CategoryEntity::class],
    version = 5
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
}

