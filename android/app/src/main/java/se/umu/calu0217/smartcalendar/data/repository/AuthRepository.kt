package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.api.AuthApi
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.domain.ChangeEmailRequest
import se.umu.calu0217.smartcalendar.domain.ChangePasswordRequest
import se.umu.calu0217.smartcalendar.domain.DeleteAccountRequest
import se.umu.calu0217.smartcalendar.domain.LoginRequest

class AuthRepository(context: Context) {
    private val dataStore = TokenDataStore(context)
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).build()

    private val api: AuthApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/api/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(AuthApi::class.java)

    val token: Flow<String?> = dataStore.token

    suspend fun login(email: String, password: String): Boolean {
        return try {
            val response = api.login(LoginRequest(email, password))
            dataStore.saveToken(response.accessToken)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changeEmail(newEmail: String, password: String): Boolean {
        val token = dataStore.getToken() ?: return false
        return try {
            api.changeEmail("Bearer $token", ChangeEmailRequest(newEmail, password))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        val token = dataStore.getToken() ?: return false
        return try {
            api.changePassword("Bearer $token", ChangePasswordRequest(oldPassword, newPassword))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAccount(password: String): Boolean {
        val token = dataStore.getToken() ?: return false
        return try {
            api.deleteAccount("Bearer $token", DeleteAccountRequest(password))
            logout()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        val current = dataStore.getToken()
        if (current != null) {
            try {
                api.logout("Bearer $current")
            } catch (_: Exception) {
            }
        }
        dataStore.clear()
        db.clearAllTables()
    }
}

