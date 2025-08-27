package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.api.CategoryApi
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.data.db.CategoryEntity
import se.umu.calu0217.smartcalendar.domain.CategoryDTO
import se.umu.calu0217.smartcalendar.domain.CreateCategoryRequest

class CategoryRepository(context: Context) {
    private val dataStore = TokenDataStore(context)
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).fallbackToDestructiveMigration().build()

    private val moshi = Moshi.Builder().build()

    private val api: CategoryApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(CategoryApi::class.java)

    val categories: Flow<List<CategoryEntity>> = db.categoryDao().getAll()

    suspend fun refresh(): Result<Unit> {
        val token = dataStore.getToken() ?: return Result.failure(Exception("No token"))
        return try {
            val remote = api.getAll("Bearer $token")
            db.categoryDao().clear()
            db.categoryDao().insertAll(remote.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(request: CreateCategoryRequest) {
        val token = dataStore.getToken() ?: return
        try {
            api.create("Bearer $token", request)
            refresh()
        } catch (_: Exception) {
        }
    }

    private fun CategoryDTO.toEntity() = CategoryEntity(
        id = id,
        name = name,
        color = color
    )
}

