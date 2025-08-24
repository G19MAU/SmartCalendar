package se.umu.calu0217.smartcalendar

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.ui.screens.LoginScreen

class MainActivity : ComponentActivity() {

    private lateinit var repository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = AuthRepository(this)

        lifecycleScope.launch {
            val token = repository.token.first()
            if (token != null) {
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                finish()
            } else {
                setContent {
                    LoginScreen(repository) {
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}

