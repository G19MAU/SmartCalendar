package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import se.umu.calu0217.smartcalendar.domain.CategoryDTO
import se.umu.calu0217.smartcalendar.domain.CreateCategoryRequest

interface CategoryApi {
    @GET("categories/all")
    suspend fun getAll(
        @Header("Authorization") auth: String
    ): List<CategoryDTO>

    @GET("categories/{id}")
    suspend fun getById(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    ): CategoryDTO

    @POST("categories/create")
    suspend fun create(
        @Header("Authorization") auth: String,
        @Body request: CreateCategoryRequest
    ): CategoryDTO

    @PUT("categories/edit/{id}")
    suspend fun edit(
        @Header("Authorization") auth: String,
        @Path("id") id: Int,
        @Body request: CreateCategoryRequest
    ): CategoryDTO

    @DELETE("categories/delete/{id}")
    suspend fun delete(
        @Header("Authorization") auth: String,
        @Path("id") id: Int
    )
}

