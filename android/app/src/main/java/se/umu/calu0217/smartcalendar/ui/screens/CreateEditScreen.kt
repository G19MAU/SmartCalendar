package se.umu.calu0217.smartcalendar.ui.screens

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import se.umu.calu0217.smartcalendar.data.ReminderWorker
import java.util.concurrent.TimeUnit
import se.umu.calu0217.smartcalendar.data.repository.TasksRepository
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import se.umu.calu0217.smartcalendar.ui.viewmodels.ActivitiesViewModel
import se.umu.calu0217.smartcalendar.data.db.CategoryEntity
import se.umu.calu0217.smartcalendar.ui.viewmodels.CategoriesViewModel
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import se.umu.calu0217.smartcalendar.domain.Recurrence
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateEditScreen(navController: NavController, itemId: Int? = null, type: String = "event") {
    val context = LocalContext.current
    val activitiesViewModel: ActivitiesViewModel = hiltViewModel()
    val tasksRepo = remember { TasksRepository(context) }
    val categoriesViewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.provideFactory(context))
    val categories by categoriesViewModel.categories.collectAsState()

    val isEvent = type != "task"
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf(LocalDateTime.now()) }
    var endDate by rememberSaveable { mutableStateOf(LocalDateTime.now()) }
    var dueDate by rememberSaveable { mutableStateOf(LocalDateTime.now()) }
    var selectedCategory by rememberSaveable { mutableStateOf<CategoryEntity?>(null) }
    var categoryExpanded by rememberSaveable { mutableStateOf(false) }
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var newCategoryName by rememberSaveable { mutableStateOf("") }
    var recurrence by rememberSaveable { mutableStateOf(Recurrence.NONE) }
    var recurrenceExpanded by rememberSaveable { mutableStateOf(false) }
    var coords by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }

    val fusedLocation: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(itemId, categories) {
        if (itemId != null) {
            if (isEvent) {
                activitiesViewModel.getById(itemId)?.let { activity ->
                    title = activity.title
                    description = activity.description ?: ""
                    startDate = LocalDateTime.parse(activity.startDate)
                    endDate = LocalDateTime.parse(activity.endDate)
                    activity.location?.split(",")?.map { it.toDouble() }
                        ?.let { coords = it[0] to it[1] }
                    selectedCategory = categories.firstOrNull { it.name == activity.category }
                    recurrence = Recurrence.valueOf(activity.recurrence)
                }
            } else {
                tasksRepo.getById(itemId)?.let { task ->
                    title = task.title
                    description = task.description ?: ""
                    dueDate = LocalDateTime.parse(task.dueDate)
                    task.location?.split(",")?.map { it.toDouble() }
                        ?.let { coords = it[0] to it[1] }
                    selectedCategory = categories.firstOrNull { it.name == task.category }
                    recurrence = Recurrence.valueOf(task.recurrence)
                }
            }
        }
    }

    var speechTarget by remember { mutableStateOf<(String) -> Unit>({}) }
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (text != null) speechTarget(text)
        }
    }
val notificationPermissionLauncher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission()
) { /* no-op */ }

LaunchedEffect(Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted &&
            (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
             ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        ) {
            fusedLocation.lastLocation.addOnSuccessListener { loc ->
                loc?.let { coords = it.latitude to it.longitude }
            }
        }
    }

    fun save() {
        scope.launch {
            if (isEvent) {
                val request = CreateActivityRequest(
                    title = title,
                    description = description.ifBlank { null },
                    location = coords?.let { "${it.first},${it.second}" },
                    startDate = startDate,
                    endDate = endDate,
                    categoryId = selectedCategory?.id ?: 0,
                    recurrence = recurrence
                )
                if (itemId != null) {
                    activitiesViewModel.edit(itemId, request)
                } else {
                    activitiesViewModel.create(request)
                }
            } else {
                val request = CreateTaskRequest(
                    title = title,
                    description = description.ifBlank { null },
                    location = coords?.let { "${it.first},${it.second}" },
                    dueDate = dueDate,
                    categoryId = selectedCategory?.id ?: 0,
                    recurrence = recurrence
                )
                if (itemId != null) {
                    tasksRepo.edit(itemId, request)
                } else {
                    tasksRepo.create(request)
                }
            }
            val data = workDataOf("title" to title)
            val delay = computeDelayFrom(if (isEvent) startDate else dueDate)
            val request = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(data)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(context).enqueue(request)
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when {
                            itemId != null && isEvent -> "Edit Event"
                            itemId != null && !isEvent -> "Edit Task"
                            isEvent -> "Create Event"
                            else -> "Create Task"
                        }
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { save() }) { Text("Save") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        speechTarget = { title = it }
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        speechLauncher.launch(intent)
                    }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Mic")
                    }
                }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
                        speechTarget = { description = it }
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        }
                        speechLauncher.launch(intent)
                    }) {
                        Icon(Icons.Filled.Mic, contentDescription = "Mic")
                    }
                }
            )

            fun pickDateTime(current: LocalDateTime, onSelected: (LocalDateTime) -> Unit) {
                DatePickerDialog(
                    context,
                    { _, y, m, d ->
                        TimePickerDialog(
                            context,
                            { _, h, min -> onSelected(LocalDateTime.of(y, m + 1, d, h, min)) },
                            current.hour,
                            current.minute,
                            true
                        ).show()
                    },
                    current.year,
                    current.monthValue - 1,
                    current.dayOfMonth
                ).show()
            }

            if (isEvent) {
                val formattedStart = remember(startDate) {
                    startDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                }
                OutlinedTextField(
                    value = formattedStart,
                    onValueChange = {},
                    label = { Text("Start") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickDateTime(startDate) { startDate = it } },
                    readOnly = true
                )

                val formattedEnd = remember(endDate) {
                    endDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                }
                OutlinedTextField(
                    value = formattedEnd,
                    onValueChange = {},
                    label = { Text("End") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickDateTime(endDate) { endDate = it } },
                    readOnly = true
                )
            } else {
                val formattedDue = remember(dueDate) {
                    dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                }
                OutlinedTextField(
                    value = formattedDue,
                    onValueChange = {},
                    label = { Text("Due Date & Time") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pickDateTime(dueDate) { dueDate = it } },
                    readOnly = true
                )
            }

            Box {
                OutlinedTextField(
                    value = selectedCategory?.name ?: "",
                    onValueChange = {},
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryExpanded = true },
                    readOnly = true
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(onClick = {
                            selectedCategory = category
                            categoryExpanded = false
                        }) {
                            Text(category.name)
                        }
                    }
                    Divider()
                    DropdownMenuItem(onClick = {
                        categoryExpanded = false
                        showCategoryDialog = true
                    }) {
                        Text("Add Category")
                    }
                }
            }

            if (showCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showCategoryDialog = false },
                    title = { Text("New Category") },
                    text = {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = { Text("Name") }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            categoriesViewModel.create(newCategoryName, null)
                            newCategoryName = ""
                            showCategoryDialog = false
                        }) { Text("Create") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCategoryDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Box {
                OutlinedTextField(
                    value = recurrence.name,
                    onValueChange = {},
                    label = { Text("Recurrence") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { recurrenceExpanded = true },
                    readOnly = true
                )
                DropdownMenu(
                    expanded = recurrenceExpanded,
                    onDismissRequest = { recurrenceExpanded = false }
                ) {
                    Recurrence.values().forEach { option ->
                        DropdownMenuItem(onClick = {
                            recurrence = option
                            recurrenceExpanded = false
                        }) {
                            Text(option.name)
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                coords?.let { (lat, lng) ->
                    Text("$lat, $lng", modifier = Modifier.weight(1f))
                }
                IconButton(onClick = {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocation.lastLocation.addOnSuccessListener { loc ->
                            loc?.let { coords = it.latitude to it.longitude }
                        }
                    } else {
                        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }) {
                    Icon(Icons.Filled.LocationOn, contentDescription = "Location")
                }
            }
        }
    }
}

private fun computeDelayFrom(dateTime: LocalDateTime): Long {
    val trigger = dateTime
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val delay = trigger - System.currentTimeMillis()
    return if (delay > 0) delay else 0
}

