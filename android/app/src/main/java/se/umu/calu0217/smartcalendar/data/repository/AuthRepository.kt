package se.umu.calu0217.smartcalendar.data.repository

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import retrofit2.Retrofit
import se.umu.calu0217.smartcalendar.data.TokenDataStore
import se.umu.calu0217.smartcalendar.data.api.AuthApi
import se.umu.calu0217.smartcalendar.data.db.AppDatabase
import se.umu.calu0217.smartcalendar.domain.ChangeEmailRequest
import se.umu.calu0217.smartcalendar.domain.ChangePasswordRequest
import se.umu.calu0217.smartcalendar.domain.DeleteAccountRequest
import se.umu.calu0217.smartcalendar.domain.LoginRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @ApplicationContext context: Context,
    retrofit: Retrofit
) {
    private val dataStore = TokenDataStore(context)
    private val db: AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "smartcalendar-db"
    ).build()

    private val api: AuthApi = retrofit.create(AuthApi::class.java)

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
        return try {
            api.changeEmail(ChangeEmailRequest(newEmail, password))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean {
        return try {
            api.changePassword(ChangePasswordRequest(oldPassword, newPassword))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteAccount(password: String): Boolean {
        return try {
            api.deleteAccount(DeleteAccountRequest(password))
            logout()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        try {
            api.logout()
        } catch (_: Exception) {
        }
        dataStore.clear()
        db.clearAllTables()
    }
}

