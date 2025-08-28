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
import se.umu.calu0217.smartcalendar.data.api.TaskApi
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.data.db.TaskEntity
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import se.umu.calu0217.smartcalendar.domain.TaskDTO
import se.umu.calu0217.smartcalendar.domain.Recurrence
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TasksRepository @Inject constructor(
    @ApplicationContext context: Context,
    retrofit: Retrofit
) {
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).fallbackToDestructiveMigration().build()

    private val workManager = WorkManager.getInstance(context)

    private val api: TaskApi = retrofit.create(TaskApi::class.java)

    val tasks: Flow<List<TaskEntity>> = db.taskDao().getAll()

    suspend fun getById(id: Int): TaskEntity? = db.taskDao().getById(id)

    suspend fun refresh(): Result<Unit> {
        return try {
            syncLocalChanges()
            val remote = api.getAll()
            db.taskDao().clear()
            db.taskDao().insertAll(remote.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(request: CreateTaskRequest) {
        try {
            val created = api.create(request)
            schedule(created)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun edit(id: Int, request: CreateTaskRequest) {
        try {
            val updated = api.edit(id, request)
            schedule(updated)
            refresh()
        } catch (_: Exception) {
            val local = db.taskDao().getById(id) ?: return
            val now = LocalDateTime.now().format(formatter)
            db.taskDao().upsert(
                local.copy(
                    title = request.title,
                    description = request.description,
                    location = request.location,
                    dueDate = request.dueDate.format(formatter),
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
            workManager.cancelUniqueWork("task-$id")
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun toggleComplete(id: Int) {
        try {
            api.toggleComplete(id)
            refresh()
        } catch (_: Exception) {
            val local = db.taskDao().getById(id) ?: return
            val now = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            db.taskDao().upsert(
                local.copy(
                    completed = !local.completed,
                    dirty = true,
                    updatedAt = now
                )
            )
        }
    }

    private suspend fun syncLocalChanges() {
        val dirty = db.taskDao().getDirty()
        for (task in dirty) {
            try {
                val request = CreateTaskRequest(
                    title = task.title,
                    description = task.description,
                    location = task.location,
                    dueDate = LocalDateTime.parse(task.dueDate),
                    categoryId = 0,
                    recurrence = Recurrence.valueOf(task.recurrence)
                )
                var updated = api.edit(task.id, request)
                if (updated.completed != task.completed) {
                    api.toggleComplete(task.id)
                    updated = api.getById(task.id)
                }
                db.taskDao().upsert(updated.toEntity())
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    val server = api.getById(task.id)
                    val localTime = task.updatedAt ?: ""
                    val serverTime = server.updatedAt ?: ""
                    if (localTime > serverTime) {
                        val request = CreateTaskRequest(
                            title = task.title,
                            description = task.description,
                            location = task.location,
                            dueDate = LocalDateTime.parse(task.dueDate),
                            categoryId = 0,
                            recurrence = Recurrence.valueOf(task.recurrence)
                        )
                        var fresh = api.edit(task.id, request)
                        if (fresh.completed != task.completed) {
                            api.toggleComplete(task.id)
                            fresh = api.getById(task.id)
                        }
                        db.taskDao().upsert(fresh.toEntity())
                    } else {
                        db.taskDao().upsert(server.toEntity())
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    private fun TaskDTO.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        location = location,
        dueDate = dueDate.format(formatter),
        completed = completed,
        category = category,
        updatedAt = updatedAt,
        dirty = false,
        recurrence = recurrence.name
    )

    private fun schedule(task: TaskDTO) {
        val trigger = task.dueDate
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val delay = trigger - System.currentTimeMillis()
        if (delay <= 0) {
            workManager.cancelUniqueWork("task-${task.id}")
            return
        }
        val data = workDataOf("title" to task.title)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        workManager.enqueueUniqueWork(
            "task-${task.id}",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}

