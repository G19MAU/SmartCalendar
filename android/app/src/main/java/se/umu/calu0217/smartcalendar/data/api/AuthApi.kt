package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import se.umu.calu0217.smartcalendar.domain.LoginRequest
import se.umu.calu0217.smartcalendar.domain.LoginResponseDTO

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponseDTO

    @POST("auth/logout")
    suspend fun logout(@Header("Authorization") auth: String)
}

