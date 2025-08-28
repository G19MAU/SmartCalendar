package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import se.umu.calu0217.smartcalendar.ui.viewmodels.ActivitiesViewModel
import se.umu.calu0217.smartcalendar.ui.viewmodels.TasksViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AgendaScreen(navController: NavController) {
    val context = LocalContext.current
    val activitiesViewModel: ActivitiesViewModel = hiltViewModel()
    val tasksViewModel: TasksViewModel =
        viewModel(factory = TasksViewModel.provideFactory(context))

    val activities by activitiesViewModel.activities.collectAsState()
    val tasks by tasksViewModel.tasks.collectAsState()
    val activitiesLoading by activitiesViewModel.isLoading.collectAsState()
    val tasksLoading by tasksViewModel.isLoading.collectAsState()
    val activityError by activitiesViewModel.error.collectAsState()
    val taskError by tasksViewModel.error.collectAsState()

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val todayActivities = remember(activities) { activities.filter { it.startDate.toLocalDate() == today } }
    val tomorrowActivities = remember(activities) { activities.filter { it.startDate.toLocalDate() == tomorrow } }

    val todayTasks = remember(tasks) { tasks.filter { it.dueDate.toLocalDate() == today } }
    val tomorrowTasks = remember(tasks) { tasks.filter { it.dueDate.toLocalDate() == tomorrow } }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(activityError, taskError) {
        val err = activityError ?: taskError
        if (err != null) {
            val result = snackbarHostState.showSnackbar(
                message = err.message ?: "Failed to load agenda",
                actionLabel = "Retry"
            )
            if (result == SnackbarResult.ActionPerformed) {
                activitiesViewModel.refresh()
                tasksViewModel.refresh()
            }
            activitiesViewModel.clearError()
            tasksViewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Today", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(todayActivities) { activity ->
            AgendaItemCard(
                title = activity.title,
                time = "${activity.startDate.toLocalTime()} - ${activity.endDate.toLocalTime()}",
                description = activity.description,
                color = activity.category?.toColor() ?: Color.Gray,
                onClick = { navController.navigate("activity/${activity.id}") }
            )
        }
        items(todayTasks) { task ->
            AgendaItemCard(
                title = task.title,
                time = task.dueDate.toLocalTime(),
                description = task.description,
                color = task.category?.toColor() ?: Color.Gray,
                onClick = { navController.navigate("task/${task.id}") }
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { Text("Tomorrow", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
        items(tomorrowActivities) { activity ->
            AgendaItemCard(
                title = activity.title,
                time = "${activity.startDate.toLocalTime()} - ${activity.endDate.toLocalTime()}",
                description = activity.description,
                color = activity.category?.toColor() ?: Color.Gray,
                onClick = { navController.navigate("activity/${activity.id}") }
            )
        }
        items(tomorrowTasks) { task ->
            AgendaItemCard(
                title = task.title,
                time = task.dueDate.toLocalTime(),
                description = task.description,
                color = task.category?.toColor() ?: Color.Gray,
                onClick = { navController.navigate("task/${task.id}") }
            )
        }
            }
            if (activitiesLoading || tasksLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun AgendaItemCard(
    title: String,
    time: String,
    description: String?,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        border = BorderStroke(2.dp, color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(time, style = MaterialTheme.typography.bodyMedium)
            if (!description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private fun String.toLocalDate(): LocalDate =
    LocalDateTime.parse(this).toLocalDate()

private fun String.toLocalTime(): String =
    DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.parse(this))

private fun String.toColor(): Color =
    try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color.Gray
    }


