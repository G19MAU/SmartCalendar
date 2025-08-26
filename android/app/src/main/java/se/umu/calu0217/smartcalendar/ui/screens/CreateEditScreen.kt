package se.umu.calu0217.smartcalendar.ui.screens

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.ActivitiesRepository
import se.umu.calu0217.smartcalendar.data.repository.TasksRepository
import se.umu.calu0217.smartcalendar.domain.CreateActivityRequest
import se.umu.calu0217.smartcalendar.domain.CreateTaskRequest
import java.time.LocalDateTime
import java.time.ZoneId
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

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fusedLocation.lastLocation.addOnSuccessListener { loc ->
                if (loc != null) coords = loc.latitude to loc.longitude
            }
        }
    }

    fun scheduleNotification(title: String, time: LocalDateTime) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", title)
        }
        val pending = PendingIntent.getBroadcast(
            context,
            title.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val millis = time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pending)
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
                scheduleNotification(title, dateTime)
            } else {
                val request = CreateTaskRequest(
                    title = title,
                    description = description.ifBlank { null },
                    dueDate = dateTime.format(formatter),
                    categoryId = categoryId.toIntOrNull() ?: 0
                )
                tasksRepo.create(request)
                scheduleNotification(title, dateTime)
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
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        fusedLocation.lastLocation.addOnSuccessListener { loc ->
                            if (loc != null) coords = loc.latitude to loc.longitude
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

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminders"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Reminders", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .build()
        notificationManager.notify(title.hashCode(), notification)
    }
}
