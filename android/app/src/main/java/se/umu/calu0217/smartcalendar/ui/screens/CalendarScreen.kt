package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import se.umu.calu0217.smartcalendar.data.db.ActivityEntity
import se.umu.calu0217.smartcalendar.ui.viewmodels.ActivitiesViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val activitiesViewModel: ActivitiesViewModel =
        viewModel(factory = ActivitiesViewModel.provideFactory(context))
    val activities by activitiesViewModel.activities.collectAsState()
    val isLoading by activitiesViewModel.isLoading.collectAsState()
    val error by activitiesViewModel.error.collectAsState()

    var scale by remember { mutableStateOf(1f) }
    var level by rememberSaveable { mutableStateOf(ZoomLevel.MONTH) }

    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    val selectedActivities = remember(activities, selectedDate) {
        activities.filter { it.startDate.toLocalDate() == selectedDate }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            val result = snackbarHostState.showSnackbar(
                message = it.message ?: "Failed to load activities",
                actionLabel = "Retry"
            )
            if (result == SnackbarResult.ActionPerformed) {
                activitiesViewModel.refresh()
            }
            activitiesViewModel.clearError()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 2f)
                        }
                        level = when {
                            scale < 0.8f -> ZoomLevel.MONTH
                            scale < 1.5f -> ZoomLevel.WEEK
                            else -> ZoomLevel.DAY
                        }
                    }
                }
                .padding(4.dp)
        ) {
            when (level) {
                ZoomLevel.MONTH -> MonthView(selectedDate, activities) {
                    selectedDate = it
                    level = ZoomLevel.DAY
                }
                ZoomLevel.WEEK -> WeekView(selectedDate, activities) {
                    selectedDate = it
                    level = ZoomLevel.DAY
                }
                ZoomLevel.DAY -> DayView(selectedDate, activities)
            }
        }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Text(
                text = "Agenda for $selectedDate",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(selectedActivities) { activity ->
                    Text(
                        text = "${activity.startDate.toLocalTime()} - ${activity.title}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
}

enum class ZoomLevel { MONTH, WEEK, DAY }

@Composable
private fun MonthView(current: LocalDate, activities: List<ActivityEntity>, onDayClick: (LocalDate) -> Unit) {
    val firstOfMonth = current.withDayOfMonth(1)
    val firstDayOfWeek = firstOfMonth.dayOfWeek.ordinal // Monday = 0
    val daysInMonth = current.lengthOfMonth()

    val days = remember(firstOfMonth) {
        buildList {
            for (i in firstDayOfWeek downTo 1) {
                add(firstOfMonth.minusDays(i.toLong()))
            }
            for (i in 0 until daysInMonth) {
                add(firstOfMonth.plusDays(i.toLong()))
            }
            while (size % 7 != 0) add(last().plusDays(1))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.weight(1f)) {
                week.forEach { day ->
                    DayCell(
                        day,
                        activities,
                        modifier = Modifier.weight(1f),
                        onClick = onDayClick
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekView(current: LocalDate, activities: List<ActivityEntity>, onDayClick: (LocalDate) -> Unit) {
    val startOfWeek = current.with(DayOfWeek.MONDAY)
    Row(modifier = Modifier.fillMaxSize()) {
        for (i in 0 until 7) {
            val day = startOfWeek.plusDays(i.toLong())
            Column(modifier = Modifier.weight(1f)) {
                DayCell(day, activities, modifier = Modifier.fillMaxWidth(), onClick = onDayClick)
                DayTimeline(day, activities, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DayView(current: LocalDate, activities: List<ActivityEntity>) {
    Column(modifier = Modifier.fillMaxSize()) {
        DayCell(current, activities, modifier = Modifier.fillMaxWidth())
        DayTimeline(current, activities, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    activities: List<ActivityEntity>,
    modifier: Modifier = Modifier,
    onClick: (LocalDate) -> Unit = {}
) {
    val isToday = date == LocalDate.now()
    val count = remember(activities, date) { activities.count { it.startDate.toLocalDate() == date } }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .border(1.dp, Color.LightGray)
            .background(if (isToday) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent)
            .clickable { onClick(date) },
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(4.dp),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )

        if (count > 0) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
            )
        }
    }
}

@Composable
private fun DayTimeline(day: LocalDate, activities: List<ActivityEntity>, modifier: Modifier = Modifier) {
    val dayActivities = remember(activities, day) { activities.filter { it.startDate.toLocalDate() == day } }
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(24) { hour ->
            val hourActivities = dayActivities.filter { LocalDateTime.parse(it.startDate).hour == hour }
            Row(modifier = Modifier.fillMaxWidth().height(40.dp)) {
                Text(
                    text = String.format("%02d:00", hour),
                    modifier = Modifier.width(40.dp)
                )
                Column {
                    hourActivities.forEach { activity ->
                        Text(activity.title, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

private fun String.toLocalDate(): LocalDate = LocalDateTime.parse(this).toLocalDate()
private fun String.toLocalTime(): String = LocalDateTime.parse(this).toLocalTime().toString()
