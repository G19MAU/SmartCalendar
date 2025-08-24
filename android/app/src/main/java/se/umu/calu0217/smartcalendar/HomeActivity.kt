package se.umu.calu0217.smartcalendar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.ui.screens.HomeScreen

class HomeActivity : ComponentActivity() {
    private val repository = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen(repository) {
                repository.logout()
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
