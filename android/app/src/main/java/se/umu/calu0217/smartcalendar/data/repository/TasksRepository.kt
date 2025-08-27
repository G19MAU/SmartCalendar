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

class TasksRepository(context: Context) {
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

    private val api: TaskApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(TaskApi::class.java)

    val tasks: Flow<List<TaskEntity>> = db.taskDao().getAll()

    suspend fun getById(id: Int): TaskEntity? = db.taskDao().getById(id)

    suspend fun refresh(): Result<Unit> {
        val token = dataStore.getToken() ?: return Result.failure(Exception("No token"))
        return try {
            syncLocalChanges()
            val remote = api.getAll("Bearer $token")
            db.taskDao().clear()
            db.taskDao().insertAll(remote.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(request: CreateTaskRequest) {
        val token = dataStore.getToken() ?: return
        try {
            val created = api.create("Bearer $token", request)
            schedule(created)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun edit(id: Int, request: CreateTaskRequest) {
        val token = dataStore.getToken() ?: return
        try {
            val updated = api.edit("Bearer $token", id, request)
            schedule(updated)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun delete(id: Int) {
        val token = dataStore.getToken() ?: return
        try {
            api.delete("Bearer $token", id)
            workManager.cancelUniqueWork("task-$id")
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun toggleComplete(id: Int) {
        val token = dataStore.getToken() ?: return
        try {
            api.toggleComplete("Bearer $token", id)
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
        val token = dataStore.getToken() ?: return
        val dirty = db.taskDao().getDirty()
        for (task in dirty) {
            try {
                api.toggleComplete("Bearer $token", task.id)
                val fresh = api.getById("Bearer $token", task.id)
                db.taskDao().upsert(fresh.toEntity())
            } catch (e: HttpException) {
                if (e.code() == 409) {
                    val server = api.getById("Bearer $token", task.id)
                    val localTime = task.updatedAt ?: ""
                    val serverTime = server.updatedAt ?: ""
                    if (localTime > serverTime) {
                        api.toggleComplete("Bearer $token", task.id)
                        val fresh = api.getById("Bearer $token", task.id)
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

