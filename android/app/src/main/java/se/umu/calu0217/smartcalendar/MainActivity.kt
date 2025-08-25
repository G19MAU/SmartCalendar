package se.umu.calu0217.smartcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import se.umu.calu0217.smartcalendar.ui.theme.SmartCalendarTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartCalendarTheme {
                SmartCalendarApp()
            }
        }
    }
}

