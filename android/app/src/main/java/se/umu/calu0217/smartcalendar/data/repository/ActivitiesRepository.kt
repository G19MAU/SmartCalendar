package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import retrofit2.Retrofit
import se.umu.calu0217.smartcalendar.data.ReminderWorker
import se.umu.calu0217.smartcalendar.data.api.ActivityApi
import se.umu.calu0217.smartcalendar.data.db.ActivityEntity
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.domain.ActivityDTO
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest
import se.umu.calu0217.smartcalendar.domain.Recurrence
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ActivitiesRepository @Inject constructor(
    @ApplicationContext context: Context,
    retrofit: Retrofit
) {
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).fallbackToDestructiveMigration().build()

    private val workManager = WorkManager.getInstance(context)

    private val api: ActivityApi = retrofit.create(ActivityApi::class.java)

    val activities: Flow<List<ActivityEntity>> = db.activityDao().getAll()

    suspend fun getById(id: Int): ActivityEntity? = db.activityDao().getById(id)

    suspend fun refresh(): Result<Unit> {
        return try {
            syncLocalChanges()
            val remote = api.getOngoing() + api.getFuture()
            val expanded = remote.flatMap { it.expand() }
            db.activityDao().clear()
            db.activityDao().insertAll(expanded.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(request: CreateActivityRequest) {
        try {
            val created = api.create(request)
            schedule(created)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun edit(id: Int, request: CreateActivityRequest) {
        try {
            val updated = api.edit(id, request)
            schedule(updated)
            refresh()
        } catch (_: Exception) {
            val local = db.activityDao().getById(id) ?: return
            val now = java.time.LocalDateTime.now().format(formatter)
            db.activityDao().upsert(
                local.copy(
                    title = request.title,
                    description = request.description,
                    location = request.location,
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
        try {
            api.delete(id)
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
        location = location,
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
        val dirty = db.activityDao().getDirty()
        for (activity in dirty) {
            try {
                val request = CreateActivityRequest(
                    title = activity.title,
                    description = activity.description,
                    location = activity.location,
                    startDate = java.time.LocalDateTime.parse(activity.startDate),
                    endDate = java.time.LocalDateTime.parse(activity.endDate),
                    categoryId = 0,
                    recurrence = Recurrence.valueOf(activity.recurrence)
                )
                api.edit(activity.id, request)
                val fresh = api.getById(activity.id)
                db.activityDao().upsert(fresh.toEntity())
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    val server = api.getById(activity.id)
                    val localTime = activity.updatedAt ?: ""
                    val serverTime = server.updatedAt ?: ""
                    if (localTime > serverTime) {
                        val request = CreateActivityRequest(
                            title = activity.title,
                            description = activity.description,
                            location = activity.location,
                            startDate = java.time.LocalDateTime.parse(activity.startDate),
                            endDate = java.time.LocalDateTime.parse(activity.endDate),
                            categoryId = 0,
                            recurrence = Recurrence.valueOf(activity.recurrence)
                        )
                        api.edit(activity.id, request)
                        val fresh = api.getById(activity.id)
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

