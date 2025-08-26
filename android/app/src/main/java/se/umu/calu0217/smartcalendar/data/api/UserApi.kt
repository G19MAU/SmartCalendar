package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.GET
import retrofit2.http.Header
import se.umu.calu0217.smartcalendar.domain.ActivityStatsDTO
import se.umu.calu0217.smartcalendar.domain.TaskStatsDTO
import se.umu.calu0217.smartcalendar.domain.UserDTO

interface UserApi {
    @GET("user/me")
    suspend fun getMe(@Header("Authorization") auth: String): UserDTO

    @GET("user/me/stats/tasks")
    suspend fun getTaskStats(@Header("Authorization") auth: String): TaskStatsDTO

    @GET("user/me/stats/activities")
    suspend fun getActivityStats(@Header("Authorization") auth: String): ActivityStatsDTO
}

