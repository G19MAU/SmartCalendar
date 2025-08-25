package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.api.TaskApi
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.data.db.TaskEntity
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import se.umu.calu0217.smartcalendar.domain.TaskDTO

class TasksRepository(context: Context) {
    private val dataStore = TokenDataStore(context)
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).build()

    private val api: TaskApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(TaskApi::class.java)

    val tasks: Flow<List<TaskEntity>> = db.taskDao().getAll()

    suspend fun refresh() {
        val token = dataStore.getToken() ?: return
        try {
            val remote = api.getAll("Bearer $token")
            db.taskDao().clear()
            db.taskDao().insertAll(remote.map { it.toEntity() })
        } catch (_: Exception) {
        }
    }

    suspend fun create(request: CreateTaskRequest) {
        val token = dataStore.getToken() ?: return
        try {
            api.create("Bearer $token", request)
            refresh()
        } catch (_: Exception) {
        }
    }

    suspend fun edit(id: Int, request: CreateTaskRequest) {
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

    suspend fun toggleComplete(id: Int) {
        val token = dataStore.getToken() ?: return
        try {
            api.toggleComplete("Bearer $token", id)
            refresh()
        } catch (_: Exception) {
        }
    }

    private fun TaskDTO.toEntity() = TaskEntity(
        id = id,
        title = title,
        description = description,
        dueDate = dueDate,
        completed = completed,
        category = category
    )
}

