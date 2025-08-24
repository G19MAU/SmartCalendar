package se.umu.calu0217.smartcalendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.ui.screens.HomeScreen
import se.umu.calu0217.smartcalendar.ui.screens.LoginScreen

@Composable
fun SmartCalendarApp() {
    val context = LocalContext.current
    val repository = remember { AuthRepository(context) }
    val token by repository.token.collectAsState(initial = null)
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = if (token != null) "home" else "login"
    ) {
        composable("login") {
            LoginScreen(repository) {
                navController.navigate("home") {
                    popUpTo("login") { inclusive = true }
                }
            }
        }
        composable("home") {
            HomeScreen(repository) {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }
}

