package se.umu.calu0217.smartcalendar.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenDataStore(private val context: Context) {
    private val tokenKey = stringPreferencesKey("token")

    val token: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[tokenKey]
    }

    suspend fun saveToken(value: String) {
        context.dataStore.edit { prefs ->
            prefs[tokenKey] = value
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    suspend fun getToken(): String? = token.first()
}

