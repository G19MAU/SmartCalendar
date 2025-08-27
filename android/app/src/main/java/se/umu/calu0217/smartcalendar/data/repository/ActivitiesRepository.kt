package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import com.squareup.moshi.Moshi
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.ReminderWorker
import se.umu.calu0217.smartcalendar.data.LocalDateTimeAdapter
import se.umu.calu0217.smartcalendar.data.api.ActivityApi
import se.umu.calu0217.smartcalendar.data.db.ActivityEntity
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.domain.ActivityDTO
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest
import se.umu.calu0217.smartcalendar.domain.Recurrence
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

class ActivitiesRepository(context: Context) {
    private val dataStore = TokenDataStore(context)
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).fallbackToDestructiveMigration().build()

    private val workManager = WorkManager.getInstance(context)

    private val moshi = Moshi.Builder()
        .add(LocalDateTimeAdapter())
        .build()

    private val api: ActivityApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(ActivityApi::class.java)

    val activities: Flow<List<ActivityEntity>> = db.activityDao().getAll()

    suspend fun refresh() {
        val token = dataStore.getToken() ?: return
        try {
            syncLocalChanges()
            val remote = api.getOngoing("Bearer $token") + api.getFuture("Bearer $token")
            val expanded = remote.flatMap { it.expand() }
            db.activityDao().clear()
            db.activityDao().insertAll(expanded.map { it.toEntity() })
        } catch (_: Exception) {
        }
    }

    suspend fun create(request: CreateActivityRequest) {
        val token = dataStore.getToken() ?: return
        try {
            val created = api.create("Bearer $token", request)
            schedule(created)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun edit(id: Int, request: CreateActivityRequest) {
        val token = dataStore.getToken() ?: return
        try {
            val updated = api.edit("Bearer $token", id, request)
            schedule(updated)
            refresh()
        } catch (_: Exception) {
            val local = db.activityDao().getById(id) ?: return
            val now = java.time.LocalDateTime.now().format(formatter)
            db.activityDao().upsert(
                local.copy(
                    title = request.title,
                    description = request.description,
                    startDate = request.startDate.format(formatter),
                    endDate = request.endDate.format(formatter),
                    recurrence = request.recurrence.name,
                    dirty = true,
                    updatedAt = now
                )
            )
        }
    }

    suspend fun delete(id: Int) {
        val token = dataStore.getToken() ?: return
        try {
            api.delete("Bearer $token", id)
            workManager.cancelUniqueWork("activity-$id")
            refresh()
        } catch (_: Exception) {
        }
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun ActivityDTO.toEntity() = ActivityEntity(
        id = id,
        title = title,
        description = description,
        startDate = startDate.format(formatter),
        endDate = endDate.format(formatter),
        category = category,
        recurrence = recurrence.name,
        updatedAt = updatedAt,
        dirty = false
    )

    private fun ActivityDTO.expand(): List<ActivityDTO> {
        if (recurrence == Recurrence.NONE) return listOf(this)
        val result = mutableListOf<ActivityDTO>()
        var start = startDate
        var end = endDate
        var i = 0
        val limit = java.time.LocalDateTime.now().plusMonths(1)
        while (start.isBefore(limit)) {
            result.add(copy(id = id * 1000 + i, startDate = start, endDate = end))
            when (recurrence) {
                Recurrence.DAILY -> {
                    start = start.plusDays(1)
                    end = end.plusDays(1)
                }
                Recurrence.WEEKLY -> {
                    start = start.plusWeeks(1)
                    end = end.plusWeeks(1)
                }
                Recurrence.MONTHLY -> {
                    start = start.plusMonths(1)
                    end = end.plusMonths(1)
                }
                Recurrence.YEARLY -> {
                    start = start.plusYears(1)
                    end = end.plusYears(1)
                }
                Recurrence.NONE -> break
            }
            i++
        }
        return result
    }

    private fun schedule(activity: ActivityDTO) {
        val trigger = activity.startDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delay = trigger - System.currentTimeMillis()
        if (delay <= 0) {
            workManager.cancelUniqueWork("activity-${activity.id}")
            return
        }
        val data = workDataOf("title" to activity.title)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            "activity-${activity.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private suspend fun syncLocalChanges() {
        val token = dataStore.getToken() ?: return
        val dirty = db.activityDao().getDirty()
        for (activity in dirty) {
            try {
                val request = CreateActivityRequest(
                    title = activity.title,
                    description = activity.description,
                    startDate = java.time.LocalDateTime.parse(activity.startDate),
                    endDate = java.time.LocalDateTime.parse(activity.endDate),
                    categoryId = 0,
                    recurrence = Recurrence.valueOf(activity.recurrence)
                )
                api.edit("Bearer $token", activity.id, request)
                val fresh = api.getById("Bearer $token", activity.id)
                db.activityDao().upsert(fresh.toEntity())
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    val server = api.getById("Bearer $token", activity.id)
                    val localTime = activity.updatedAt ?: ""
                    val serverTime = server.updatedAt ?: ""
                    if (localTime > serverTime) {
                        val request = CreateActivityRequest(
                            title = activity.title,
                            description = activity.description,
                            startDate = java.time.LocalDateTime.parse(activity.startDate),
                            endDate = java.time.LocalDateTime.parse(activity.endDate),
                            categoryId = 0,
                            recurrence = Recurrence.valueOf(activity.recurrence)
                        )
                        api.edit("Bearer $token", activity.id, request)
                        val fresh = api.getById("Bearer $token", activity.id)
                        db.activityDao().upsert(fresh.toEntity())
                    } else {
                        db.activityDao().upsert(server.toEntity())
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}

