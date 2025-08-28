package se.umu.calu0217.smartcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.data.repository.UserRepository
import se.umu.calu0217.smartcalendar.ui.theme.SmartCalendarTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCalendarTheme {
                SmartCalendarApp(authRepository, userRepository)
            }
        }
    }
}

