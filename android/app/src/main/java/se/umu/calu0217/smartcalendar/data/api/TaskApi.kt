package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import se.umu.calu0217.smartcalendar.domain.ConvertTaskRequest
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import se.umu.calu0217.smartcalendar.domain.TaskDTO

interface TaskApi {
    @GET("tasks/all")
    suspend fun getAll(): List<TaskDTO>

    @GET("tasks/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): TaskDTO

    @POST("tasks/create")
    suspend fun create(
        @Body request: CreateTaskRequest
    ): TaskDTO

    @PUT("tasks/edit/{id}")
    suspend fun edit(
        @Path("id") id: Int,
        @Body request: CreateTaskRequest
    ): TaskDTO

    @DELETE("tasks/delete/{id}")
    suspend fun delete(
        @Path("id") id: Int
    )

    @PUT("tasks/{id}/complete")
    suspend fun toggleComplete(
        @Path("id") id: Int
    )

    @GET("tasks/category/{categoryId}")
    suspend fun getByCategory(
        @Path("categoryId") categoryId: Int
    ): List<TaskDTO>

    @POST("tasks/convert/{id}")
    suspend fun convert(
        @Path("id") id: Int,
        @Body request: ConvertTaskRequest
    )
}

