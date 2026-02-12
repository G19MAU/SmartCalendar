package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.db.TaskEntity
import se.umu.calu0217.smartcalendar.ui.viewmodels.TasksViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(navController: NavController) {
    val context = LocalContext.current
    val viewModel: TasksViewModel =
        viewModel(factory = TasksViewModel.provideFactory(context))

    val tasks by viewModel.tasks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isLoading,
        onRefresh = { viewModel.refresh() }
    )

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            val result = snackbarHostState.showSnackbar(
                message = it.message ?: "Failed to load tasks",
                actionLabel = "Retry"
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.refresh()
            }
            viewModel.clearError()
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Status", fontWeight = FontWeight.SemiBold)
                StatusFilter.values().forEach { status ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                statusFilter = status
                                scope.launch {
                                    sheetState.hide()
                                    showSheet = false
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = statusFilter == status,
                            onClick = {
                                statusFilter = status
                                scope.launch {
                                    sheetState.hide()
                                    showSheet = false
                                }
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
                                scope.launch {
                                    sheetState.hide()
                                    showSheet = false
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = categoryFilter == cat,
                            onClick = {
                                categoryFilter = if (categoryFilter == cat) null else cat
                                scope.launch {
                                    sheetState.hide()
                                    showSheet = false
                                }
                            }
                        )
                        Text(cat)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Todos") },
                actions = {
                    IconButton(onClick = {
                        showSheet = true
                        scope.launch { sheetState.show() }
                    }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                refreshing = isLoading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!task.description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(task.description, style = MaterialTheme.typography.bodyMedium)
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
