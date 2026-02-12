package se.umu.calu0217.smartcalendar.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities")
    fun getAll(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): ActivityEntity?

    @Query("SELECT * FROM activities WHERE dirty = 1")
    suspend fun getDirty(): List<ActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(values: List<ActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(value: ActivityEntity)

    @Query("DELETE FROM activities")
    suspend fun clear()
}

