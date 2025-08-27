package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import se.umu.calu0217.smartcalendar.data.repository.ActivitiesRepository
import se.umu.calu0217.smartcalendar.ui.viewmodels.ActivitiesViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun ActivityDetailScreen(navController: NavController, activityId: Int) {
    val context = LocalContext.current
    val viewModel: ActivitiesViewModel =
        viewModel(factory = ActivitiesViewModel.provideFactory(context))
    val activities by viewModel.activities.collectAsState()
    val activity = activities.firstOrNull { it.id == activityId }
    val repo = remember { ActivitiesRepository(context) }
    val scope = rememberCoroutineScope()

    if (activity == null) {
        Text("Loading...", modifier = Modifier.padding(16.dp))
        return
    }

    val formatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val start = LocalDateTime.parse(activity.startDate).format(formatter)
    val end = LocalDateTime.parse(activity.endDate).format(formatter)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(activity.title, style = MaterialTheme.typography.h4, fontWeight = FontWeight.Bold)
        activity.description?.let { Text(it) }
        Text("Start: $start")
        Text("End: $end")
        Text("Location: Not specified")
        Text("Category: ${activity.category ?: "None"}")
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { navController.navigate("edit?itemId=${activity.id}") }) {
                Text("Edit")
            }
            Button(
                onClick = {
                    scope.launch {
                        repo.delete(activity.id)
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Delete")
            }
        }
    }
}

