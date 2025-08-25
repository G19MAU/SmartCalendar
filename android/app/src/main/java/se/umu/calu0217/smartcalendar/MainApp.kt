package se.umu.calu0217.smartcalendar

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.ui.components.BottomNavBar
import se.umu.calu0217.smartcalendar.ui.screens.AgendaScreen
import se.umu.calu0217.smartcalendar.ui.screens.CalendarScreen
import se.umu.calu0217.smartcalendar.ui.screens.SettingsScreen
import se.umu.calu0217.smartcalendar.ui.screens.TodoScreen
import se.umu.calu0217.smartcalendar.ui.screens.LoginScreen

@Composable
fun SmartCalendarApp() {
    val context = LocalContext.current
    val repository = remember { AuthRepository(context) }
    val token by repository.token.collectAsState(initial = null)
    val navController = rememberNavController()

    if (token == null) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(repository) {
                    navController.navigate("agenda") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            }
        }
    } else {
        BottomNavBar(navController) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "agenda",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("agenda") { AgendaScreen() }
                composable("calendar") { CalendarScreen() }
                composable("todos") { TodoScreen() }
                composable("settings") { SettingsScreen() }
                composable("logout") {
                    LaunchedEffect(Unit) {
                        repository.logout()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                composable("edit") { Text("Create/Edit") }
            }
        }
    }
}

