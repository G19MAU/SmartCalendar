package se.umu.calu0217.smartcalendar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Row

private data class BottomNavItem(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val label: String)

@Composable
fun BottomNavBar(
    navController: NavController,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val items = listOf(
        BottomNavItem("agenda", Icons.Filled.ViewAgenda, "Agenda"),
        BottomNavItem("calendar", Icons.Filled.CalendarToday, "Calendar"),
        BottomNavItem("todos", Icons.Filled.Check, "Todos"),
        BottomNavItem("settings", Icons.Filled.Settings, "Settings"),
        BottomNavItem("logout", Icons.Filled.ExitToApp, "Logout")
    )

    Scaffold(
        bottomBar = {
            BottomAppBar(cutoutShape = androidx.compose.foundation.shape.CircleShape) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    items.forEach { item ->
                        IconButton(
                            onClick = { navController.navigate(item.route) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label
                                )
                                Text(
                                    text = item.label,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("edit") }) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        isFloatingActionButtonDocked = true,
        content = content
    )
}

