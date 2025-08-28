package se.umu.calu0217.smartcalendar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.data.repository.UserRepository
import se.umu.calu0217.smartcalendar.ui.components.BottomNavBar
import se.umu.calu0217.smartcalendar.ui.screens.AgendaScreen
import se.umu.calu0217.smartcalendar.ui.screens.CalendarScreen
import se.umu.calu0217.smartcalendar.ui.screens.CreateEditScreen
import se.umu.calu0217.smartcalendar.ui.screens.SettingsScreen
import se.umu.calu0217.smartcalendar.ui.screens.TodoScreen
import se.umu.calu0217.smartcalendar.ui.screens.LoginScreen
import se.umu.calu0217.smartcalendar.ui.screens.ActivityDetailScreen
import se.umu.calu0217.smartcalendar.ui.screens.TaskDetailScreen

@Composable
fun SmartCalendarApp(
    authRepository: AuthRepository,
    userRepository: UserRepository
) {
    val token by authRepository.token.collectAsState(initial = null)
    val navController = rememberNavController()

    if (token == null) {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(authRepository) {
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
                composable("agenda") { AgendaScreen(navController) }
                composable("calendar") { CalendarScreen() }
                composable("todos") { TodoScreen(navController) }
                composable("settings") {
                    SettingsScreen(authRepository, userRepository) {
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                composable("logout") {
                    LaunchedEffect(Unit) {
                        authRepository.logout()
                        navController.navigate("login") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
                composable(
                    route = "edit?itemId={itemId}&type={type}",
                    arguments = listOf(
                        navArgument("itemId") {
                            type = NavType.IntType
                            defaultValue = -1
                        },
                        navArgument("type") {
                            type = NavType.StringType
                            defaultValue = "event"
                        }
                    )
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("itemId")?.takeIf { it != -1 }
                    val type = backStackEntry.arguments?.getString("type") ?: "event"
                    CreateEditScreen(navController, id, type)
                }
                composable(
                    route = "activity/{activityId}",
                    arguments = listOf(navArgument("activityId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("activityId") ?: return@composable
                    ActivityDetailScreen(navController, id)
                }
                composable(
                    route = "task/{taskId}",
                    arguments = listOf(navArgument("taskId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val id = backStackEntry.arguments?.getInt("taskId") ?: return@composable
                    TaskDetailScreen(navController, id)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    // Preview with dummy repositories
    val context = androidx.compose.ui.platform.LocalContext.current
    SmartCalendarApp(AuthRepository(context), UserRepository(context))
}
