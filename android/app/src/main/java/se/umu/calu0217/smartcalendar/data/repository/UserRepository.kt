package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.api.UserApi
import se.umu.calu0217.smartcalendar.domain.ActivityStatsDTO
import se.umu.calu0217.smartcalendar.domain.TaskStatsDTO
import se.umu.calu0217.smartcalendar.domain.UserDTO

class UserRepository(context: Context) {
    private val dataStore = TokenDataStore(context)

    private val api: UserApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(UserApi::class.java)

    suspend fun getMe(): UserDTO? {
        val token = dataStore.getToken() ?: return null
        return try {
            api.getMe("Bearer $token")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTaskStats(): TaskStatsDTO? {
        val token = dataStore.getToken() ?: return null
        return try {
            api.getTaskStats("Bearer $token")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getActivityStats(): ActivityStatsDTO? {
        val token = dataStore.getToken() ?: return null
        return try {
            api.getActivityStats("Bearer $token")
        } catch (e: Exception) {
            null
        }
    }
}

