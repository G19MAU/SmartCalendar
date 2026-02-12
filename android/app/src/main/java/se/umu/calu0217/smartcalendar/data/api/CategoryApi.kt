package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import se.umu.calu0217.smartcalendar.domain.CategoryDTO
import se.umu.calu0217.smartcalendar.domain.CreateCategoryRequest

interface CategoryApi {
    @GET("categories/all")
    suspend fun getAll(): List<CategoryDTO>

    @GET("categories/{id}")
    suspend fun getById(
        @Path("id") id: Int
    ): CategoryDTO

    @POST("categories/create")
    suspend fun create(
        @Body request: CreateCategoryRequest
    ): CategoryDTO

    @PUT("categories/edit/{id}")
    suspend fun edit(
        @Path("id") id: Int,
        @Body request: CreateCategoryRequest
    ): CategoryDTO

    @DELETE("categories/delete/{id}")
    suspend fun delete(
        @Path("id") id: Int
    )
}

