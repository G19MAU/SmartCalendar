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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.ActivitiesRepository
import se.umu.calu0217.smartcalendar.data.repository.TasksRepository
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import se.umu.calu0217.smartcalendar.domain.Recurrence
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateEditScreen(navController: NavController, itemId: Int? = null) {
    val context = LocalContext.current
    val activitiesRepo = remember { ActivitiesRepository(context) }
    val tasksRepo = remember { TasksRepository(context) }

    var isEvent by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDateTime.now()) }
    var endDate by remember { mutableStateOf(LocalDateTime.now()) }
    var dueDate by remember { mutableStateOf(LocalDateTime.now()) }
    var categoryId by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf(Recurrence.NONE) }
    var recurrenceExpanded by remember { mutableStateOf(false) }
    var coords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    val fusedLocation: FusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val scope = rememberCoroutineScope()

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (text != null) description = text
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
                    startDate = startDate,
                    endDate = endDate,
                    categoryId = categoryId.toIntOrNull() ?: 0,
                    recurrence = recurrence
                )
                activitiesRepo.create(request)
            } else {
                val request = CreateTaskRequest(
                    title = title,
                    description = description.ifBlank { null },
                    dueDate = dueDate,
                    categoryId = categoryId.toIntOrNull() ?: 0,
                    recurrence = recurrence
                )
                tasksRepo.create(request)
            }
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (isEvent) "Create Event" else "Create Task") })
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Event")
                Switch(checked = isEvent, onCheckedChange = { isEvent = it })
                Text("Task")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = {
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

            OutlinedTextField(
                value = categoryId,
                onValueChange = { categoryId = it },
                label = { Text("Category ID") },
                modifier = Modifier.fillMaxWidth()
            )

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

