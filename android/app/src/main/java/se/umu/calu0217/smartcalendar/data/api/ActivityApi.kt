package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import se.umu.calu0217.smartcalendar.domain.ActivityDTO
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest

interface ActivityApi {
    @GET("activities/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): ActivityDTO

    @POST("activities/create")
    suspend fun create(
        @Body request: CreateActivityRequest
    ): ActivityDTO

    @PUT("activities/edit/{id}")
    suspend fun edit(
        @Path("id") id: Int,
        @Body request: CreateActivityRequest
    ): ActivityDTO

    @DELETE("activities/delete/{id}")
    suspend fun delete(
        @Path("id") id: Int
    )

    @GET("activities/ongoing")
    suspend fun getOngoing(): List<ActivityDTO>

    @GET("activities/future")
    suspend fun getFuture(): List<ActivityDTO>

    @GET("activities/category/{categoryId}")
    suspend fun getByCategory(
        @Path("categoryId") categoryId: Int
    ): List<ActivityDTO>

    @GET("activities/between")
    suspend fun getBetween(
        @Query("start") start: String,
        @Query("end") end: String
    ): List<ActivityDTO>
}

