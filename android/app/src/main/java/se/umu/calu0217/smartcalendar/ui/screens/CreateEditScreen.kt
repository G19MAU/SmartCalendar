package se.umu.calu0217.smartcalendar.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Context
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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.ActivitiesRepository
import se.umu.calu0217.smartcalendar.data.repository.TasksRepository
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun CreateEditScreen() {
    val context = LocalContext.current
    val activitiesRepo = remember { ActivitiesRepository(context) }
    val tasksRepo = remember { TasksRepository(context) }

    var isEvent by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf(LocalDateTime.now()) }
    var categoryId by remember { mutableStateOf("") }
    var recurrence by remember { mutableStateOf("") }
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
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        scope.launch {
            if (isEvent) {
                val request = CreateActivityRequest(
                    title = title,
                    description = description.ifBlank { null },
                    startDate = dateTime.format(formatter),
                    endDate = dateTime.format(formatter),
                    categoryId = categoryId.toIntOrNull() ?: 0
                )
                activitiesRepo.create(request)
            } else {
                val request = CreateTaskRequest(
                    title = title,
                    description = description.ifBlank { null },
                    dueDate = dateTime.format(formatter),
                    categoryId = categoryId.toIntOrNull() ?: 0
                )
                tasksRepo.create(request)
            }
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

            val formatted = remember(dateTime) {
                dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            }
            OutlinedTextField(
                value = formatted,
                onValueChange = {},
                label = { Text("Date & Time") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // Show date/time pickers elsewhere
                    },
                readOnly = true
            )

            OutlinedTextField(
                value = categoryId,
                onValueChange = { categoryId = it },
                label = { Text("Category ID") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = recurrence,
                onValueChange = { recurrence = it },
                label = { Text("Recurrence") },
                modifier = Modifier.fillMaxWidth()
            )

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

