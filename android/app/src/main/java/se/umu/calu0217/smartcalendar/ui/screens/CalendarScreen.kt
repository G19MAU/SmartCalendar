package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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

    var scale by remember { mutableStateOf(1f) }
    var level by remember { mutableStateOf(ZoomLevel.MONTH) }

    val today = remember { LocalDate.now() }
    val todayActivities = remember(activities) { activities.filter { it.startDate.toLocalDate() == today } }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .pointerInput(Unit) {
                    while (true) {
                        detectTransformGestures { _, _, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 2f)
                        }
                        level = when {
                            scale < 0.8f -> ZoomLevel.MONTH
                            scale < 1.5f -> ZoomLevel.WEEK
                            else -> ZoomLevel.DAY
                        }
                        scale = when (level) {
                            ZoomLevel.MONTH -> 0.8f
                            ZoomLevel.WEEK -> 1f
                            ZoomLevel.DAY -> 1.5f
                        }
                    }
                }
                .padding(4.dp)
        ) {
            when (level) {
                ZoomLevel.MONTH -> MonthView(today)
                ZoomLevel.WEEK -> WeekView(today)
                ZoomLevel.DAY -> DayView(today)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        Text(
            text = "Today's Agenda",
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            items(todayActivities) { activity ->
                Text(
                    text = "${activity.startDate.toLocalTime()} - ${activity.title}",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

enum class ZoomLevel { MONTH, WEEK, DAY }

@Composable
private fun MonthView(current: LocalDate) {
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
                    DayCell(day, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WeekView(current: LocalDate) {
    val startOfWeek = current.with(DayOfWeek.MONDAY)
    Row(modifier = Modifier.fillMaxSize()) {
        for (i in 0 until 7) {
            DayCell(startOfWeek.plusDays(i.toLong()), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun DayView(current: LocalDate) {
    Column(modifier = Modifier.fillMaxSize()) {
        DayCell(current, modifier = Modifier.fillMaxWidth().weight(1f))
    }
}

@Composable
private fun DayCell(date: LocalDate, modifier: Modifier = Modifier) {
    val isToday = date == LocalDate.now()
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .border(1.dp, Color.LightGray)
            .background(if (isToday) MaterialTheme.colors.primary.copy(alpha = 0.3f) else Color.Transparent),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(4.dp),
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
        )
    }
}

private fun String.toLocalDate(): LocalDate = LocalDateTime.parse(this).toLocalDate()
private fun String.toLocalTime(): String = LocalDateTime.parse(this).toLocalTime().toString()
