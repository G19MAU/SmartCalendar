package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.db.TaskEntity
import se.umu.calu0217.smartcalendar.ui.viewmodels.TasksViewModel
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.navigation.NavController

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TodoScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TasksViewModel =
        viewModel(factory = TasksViewModel.provideFactory(context))

    val tasks by viewModel.tasks.collectAsState()

    var statusFilter by remember { mutableStateOf(StatusFilter.All) }
    var categoryFilter by remember { mutableStateOf<String?>(null) }

    val categories = remember(tasks) { tasks.mapNotNull { it.category }.distinct() }

    val filteredTasks = remember(tasks, statusFilter, categoryFilter) {
        tasks.filter { task ->
            val statusMatch = when (statusFilter) {
                StatusFilter.All -> true
                StatusFilter.Completed -> task.completed
                StatusFilter.Pending -> !task.completed
            }
            val categoryMatch = categoryFilter == null || task.category == categoryFilter
            statusMatch && categoryMatch
        }
    }

    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.refresh()
                isRefreshing = false
            }
        }
    )

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status", fontWeight = FontWeight.SemiBold)
                StatusFilter.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                statusFilter = status
                                scope.launch { sheetState.hide() }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = statusFilter == status,
                            onClick = {
                                statusFilter = status
                                scope.launch { sheetState.hide() }
                            }
                        )
                        Text(status.label)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Category", fontWeight = FontWeight.SemiBold)
                categories.forEach { cat ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                categoryFilter = if (categoryFilter == cat) null else cat
                                scope.launch { sheetState.hide() }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = categoryFilter == cat,
                            onClick = {
                                categoryFilter = if (categoryFilter == cat) null else cat
                                scope.launch { sheetState.hide() }
                            }
                        )
                        Text(cat)
                    }
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Todos") },
                    actions = {
                        IconButton(onClick = { scope.launch { sheetState.show() } }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pullRefresh(pullRefreshState)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(filteredTasks) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { viewModel.toggleComplete(task.id) },
                            onClick = { navController.navigate("task/${task.id}") }
                        )
                    }
                }
                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

private enum class StatusFilter(val label: String) {
    All("All"),
    Completed("Completed"),
    Pending("Pending")
}

@Composable
private fun TaskCard(task: TaskEntity, onToggle: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        border = BorderStroke(2.dp, task.category?.toColor() ?: Color.Gray)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.SemiBold
                )
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(task.description, style = MaterialTheme.typography.body2)
                }
            }
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

private fun String.toColor(): Color =
    try {
        Color(android.graphics.Color.parseColor(this))
    } catch (_: Exception) {
        Color.Gray
    }

