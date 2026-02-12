package se.umu.calu0217.smartcalendar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import se.umu.calu0217.smartcalendar.data.repository.TasksRepository
import se.umu.calu0217.smartcalendar.ui.viewmodels.TasksViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun TaskDetailScreen(navController: NavController, taskId: Int) {
    val context = LocalContext.current
    val viewModel: TasksViewModel =
        viewModel(factory = TasksViewModel.provideFactory(context))
    val tasks by viewModel.tasks.collectAsState()
    val task = tasks.firstOrNull { it.id == taskId }
    val repo = remember { TasksRepository(context) }
    val scope = rememberCoroutineScope()

    if (task == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val due = LocalDateTime.parse(task.dueDate).format(formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(task.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        task.description?.let { Text(it) }
        Text("Due: $due")
        task.location?.let { loc ->
            Text(
                text = "Location: $loc",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val uri = Uri.parse("geo:$loc?q=$loc")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    context.startActivity(intent)
                }
            )
        } ?: Text("Location: Not specified")
        Text("Category: ${task.category ?: "None"}")
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate("edit?itemId=${task.id}&type=task") }) {
                Text("Edit")
            }
            Button(
                onClick = {
                    scope.launch {
                        repo.delete(task.id)
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Delete")
            }
        }
    }
}

