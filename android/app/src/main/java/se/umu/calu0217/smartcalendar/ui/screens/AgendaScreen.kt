package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import se.umu.calu0217.smartcalendar.ui.viewmodels.ActivitiesViewModel
import se.umu.calu0217.smartcalendar.ui.viewmodels.TasksViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun AgendaScreen() {
    val context = LocalContext.current
    val activitiesViewModel: ActivitiesViewModel =
        viewModel(factory = ActivitiesViewModel.provideFactory(context))
    val tasksViewModel: TasksViewModel =
        viewModel(factory = TasksViewModel.provideFactory(context))

    val activities by activitiesViewModel.activities.collectAsState()
    val tasks by tasksViewModel.tasks.collectAsState()

    val today = LocalDate.now()
    val tomorrow = today.plusDays(1)

    val todayActivities = remember(activities) { activities.filter { it.startDate.toLocalDate() == today } }
    val tomorrowActivities = remember(activities) { activities.filter { it.startDate.toLocalDate() == tomorrow } }

    val todayTasks = remember(tasks) { tasks.filter { it.dueDate.toLocalDate() == today } }
    val tomorrowTasks = remember(tasks) { tasks.filter { it.dueDate.toLocalDate() == tomorrow } }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        item { Text("Today", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
        items(todayActivities) { activity ->
            AgendaItemCard(
                title = activity.title,
                time = "${activity.startDate.toLocalTime()} - ${activity.endDate.toLocalTime()}",
                description = activity.description,
                color = activity.category?.toColor() ?: Color.Gray
            )
        }
        items(todayTasks) { task ->
            AgendaItemCard(
                title = task.title,
                time = task.dueDate.toLocalTime(),
                description = task.description,
                color = task.category?.toColor() ?: Color.Gray
            )
        }
        item { Spacer(modifier = Modifier.height(24.dp)) }
        item { Text("Tomorrow", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold) }
        items(tomorrowActivities) { activity ->
            AgendaItemCard(
                title = activity.title,
                time = "${activity.startDate.toLocalTime()} - ${activity.endDate.toLocalTime()}",
                description = activity.description,
                color = activity.category?.toColor() ?: Color.Gray
            )
        }
        items(tomorrowTasks) { task ->
            AgendaItemCard(
                title = task.title,
                time = task.dueDate.toLocalTime(),
                description = task.description,
                color = task.category?.toColor() ?: Color.Gray
            )
        }
    }
}

@Composable
private fun AgendaItemCard(
    title: String,
    time: String,
    description: String?,
    color: Color
) {
    val expanded = remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { expanded.value = !expanded.value },
        border = BorderStroke(2.dp, color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold)
            Text(time, style = MaterialTheme.typography.body2)
            if (expanded.value && !description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(description, style = MaterialTheme.typography.body2)
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


