package se.umu.calu0217.smartcalendar.ui.screens

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository
import se.umu.calu0217.smartcalendar.data.repository.UserRepository
import se.umu.calu0217.smartcalendar.domain.ActivityStatsDTO
import se.umu.calu0217.smartcalendar.domain.TaskStatsDTO
import se.umu.calu0217.smartcalendar.domain.UserDTO

@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    userRepository: UserRepository,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val userState = remember { mutableStateOf<UserDTO?>(null) }
    val taskStatsState = remember { mutableStateOf<TaskStatsDTO?>(null) }
    val activityStatsState = remember { mutableStateOf<ActivityStatsDTO?>(null) }

    LaunchedEffect(Unit) {
        userState.value = userRepository.getMe()
        taskStatsState.value = userRepository.getTaskStats()
        activityStatsState.value = userRepository.getActivityStats()
    }

    val newEmail = remember { mutableStateOf("") }
    val emailPassword = remember { mutableStateOf("") }
    val oldPassword = remember { mutableStateOf("") }
    val newPassword = remember { mutableStateOf("") }
    val deletePassword = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        userState.value?.let { user ->
            val image = user.profileIcon?.let {
                try {
                    val bytes = Base64.decode(it, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            }
            image?.let {
                Image(bitmap = it, contentDescription = "Profile Icon")
                Spacer(modifier = Modifier.height(16.dp))
            }
            Text("Name: ${user.fullName}")
            Text("Email: ${user.email}")
            Spacer(modifier = Modifier.height(24.dp))
        }

        taskStatsState.value?.let { stats ->
            Text("Tasks - Total: ${stats.total}, Completed: ${stats.completed}, Pending: ${stats.pending}")
        }
        activityStatsState.value?.let { stats ->
            Text("Activities - Total: ${stats.total}, Upcoming: ${stats.upcoming}, Ongoing: ${stats.ongoing}")
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = newEmail.value,
            onValueChange = { newEmail.value = it },
            label = { Text("New Email") }
        )
        OutlinedTextField(
            value = emailPassword.value,
            onValueChange = { emailPassword.value = it },
            label = { Text("Current Password") }
        )
        Button(onClick = {
            scope.launch {
                if (authRepository.changeEmail(newEmail.value, emailPassword.value)) {
                    userState.value = userRepository.getMe()
                }
            }
        }) { Text("Change Email") }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = oldPassword.value,
            onValueChange = { oldPassword.value = it },
            label = { Text("Old Password") }
        )
        OutlinedTextField(
            value = newPassword.value,
            onValueChange = { newPassword.value = it },
            label = { Text("New Password") }
        )
        Button(onClick = {
            scope.launch { authRepository.changePassword(oldPassword.value, newPassword.value) }
        }) { Text("Change Password") }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = deletePassword.value,
            onValueChange = { deletePassword.value = it },
            label = { Text("Password to Delete Account") }
        )
        Button(onClick = {
            scope.launch {
                if (authRepository.deleteAccount(deletePassword.value)) {
                    onLogout()
                }
            }
        }) { Text("Delete Account") }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            scope.launch {
                authRepository.logout()
                onLogout()
            }
        }) { Text("Logout") }
    }
}

