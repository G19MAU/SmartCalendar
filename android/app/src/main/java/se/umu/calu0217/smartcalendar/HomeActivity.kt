package se.umu.calu0217.smartcalendar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository

class HomeActivity : AppCompatActivity() {
    private lateinit var repository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = AuthRepository(this)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.logoutButton).setOnClickListener {
            lifecycleScope.launch {
                repository.logout()
                startActivity(Intent(this@HomeActivity, MainActivity::class.java))
                finish()
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}

