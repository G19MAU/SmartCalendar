package se.umu.calu0217.smartcalendar.data.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.HTTP
import retrofit2.http.POST
import retrofit2.http.PUT
import se.umu.calu0217.smartcalendar.domain.ChangeEmailRequest
import se.umu.calu0217.smartcalendar.domain.ChangePasswordRequest
import se.umu.calu0217.smartcalendar.domain.DeleteAccountRequest
import se.umu.calu0217.smartcalendar.domain.LoginRequest
import se.umu.calu0217.smartcalendar.domain.LoginResponseDTO
import se.umu.calu0217.smartcalendar.domain.ResponseDTO

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponseDTO

    @POST("auth/logout")
    suspend fun logout()

    @PUT("auth/change-email")
    suspend fun changeEmail(
        @Body request: ChangeEmailRequest
    ): ResponseDTO

    @PUT("auth/change-password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): ResponseDTO

    @HTTP(method = "DELETE", path = "auth/delete-account", hasBody = true)
    suspend fun deleteAccount(
        @Body request: DeleteAccountRequest
    ): ResponseDTO
}

