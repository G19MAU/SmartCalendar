package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.api.ActivityApi
import se.umu.calu0217.smartcalendar.data.db.ActivityEntity
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.domain.ActivityDTO
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest

class ActivitiesRepository(context: Context) {
    private val dataStore = TokenDataStore(context)
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).build()

    private val api: ActivityApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(ActivityApi::class.java)

    val activities: Flow<List<ActivityEntity>> = db.activityDao().getAll()

    suspend fun refresh() {
        val token = dataStore.getToken() ?: return
        try {
            val remote = api.getOngoing("Bearer $token") + api.getFuture("Bearer $token")
            db.activityDao().clear()
            db.activityDao().insertAll(remote.map { it.toEntity() })
        } catch (_: Exception) {
        }
    }

    suspend fun create(request: CreateActivityRequest) {
        val token = dataStore.getToken() ?: return
        try {
            api.create("Bearer $token", request)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun edit(id: Int, request: CreateActivityRequest) {
        val token = dataStore.getToken() ?: return
        try {
            api.edit("Bearer $token", id, request)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun delete(id: Int) {
        val token = dataStore.getToken() ?: return
        try {
            api.delete("Bearer $token", id)
            refresh()
        } catch (_: Exception) {
        }
    }

    private fun ActivityDTO.toEntity() = ActivityEntity(
        id = id,
        title = title,
        description = description,
        startDate = startDate,
        endDate = endDate,
        category = category
    )
}

