package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.GET
import se.umu.calu0217.smartcalendar.domain.ActivityStatsDTO
import se.umu.calu0217.smartcalendar.domain.TaskStatsDTO
import se.umu.calu0217.smartcalendar.domain.UserDTO

interface UserApi {
    @GET("user/me")
    suspend fun getMe(): UserDTO

    @GET("user/me/stats/tasks")
    suspend fun getTaskStats(): TaskStatsDTO

    @GET("user/me/stats/activities")
    suspend fun getActivityStats(): ActivityStatsDTO
}

