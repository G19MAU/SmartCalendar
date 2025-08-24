package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository

@Composable
fun HomeScreen(
    repository: AuthRepository,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Välkommen!")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            scope.launch {
                repository.logout()
                onLogout()
            }
        }) {
            Text("Logout")
        }
    }
}

