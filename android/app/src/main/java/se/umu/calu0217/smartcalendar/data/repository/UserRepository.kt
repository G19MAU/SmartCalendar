package se.umu.calu0217.smartcalendar.data.repository

import retrofit2.Retrofit
import se.umu.calu0217.smartcalendar.data.api.UserApi
import se.umu.calu0217.smartcalendar.domain.ActivityStatsDTO
import se.umu.calu0217.smartcalendar.domain.TaskStatsDTO
import se.umu.calu0217.smartcalendar.domain.UserDTO
import javax.inject.Inject

class UserRepository @Inject constructor(
    retrofit: Retrofit
) {
    private val api: UserApi = retrofit.create(UserApi::class.java)

    suspend fun getMe(): UserDTO? {
        return try {
            api.getMe()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getTaskStats(): TaskStatsDTO? {
        return try {
            api.getTaskStats()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getActivityStats(): ActivityStatsDTO? {
        return try {
            api.getActivityStats()
        } catch (e: Exception) {
            null
        }
    }
}

