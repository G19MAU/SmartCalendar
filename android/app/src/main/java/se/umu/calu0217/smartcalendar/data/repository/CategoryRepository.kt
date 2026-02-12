package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import se.umu.calu0217.smartcalendar.data.api.CategoryApi
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.data.db.CategoryEntity
import se.umu.calu0217.smartcalendar.domain.CategoryDTO
import se.umu.calu0217.smartcalendar.domain.CreateCategoryRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CategoryRepository @Inject constructor(
    @ApplicationContext context: Context,
    retrofit: Retrofit
) {
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).fallbackToDestructiveMigration().build()

    private val api: CategoryApi = retrofit.create(CategoryApi::class.java)

    val categories: Flow<List<CategoryEntity>> = db.categoryDao().getAll()

    suspend fun refresh(): Result<Unit> {
        return try {
            val remote = api.getAll()
            db.categoryDao().clear()
            db.categoryDao().insertAll(remote.map { it.toEntity() })
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun create(request: CreateCategoryRequest) {
        try {
            api.create(request)
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

