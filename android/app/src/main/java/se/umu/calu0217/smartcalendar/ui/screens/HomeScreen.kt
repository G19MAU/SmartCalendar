package se.umu.calu0217.smartcalendar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import se.umu.calu0217.smartcalendar.data.repository.AuthRepository

@Composable
fun HomeScreen(
    repository: AuthRepository,
    onLogout: suspend () -> Unit
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Välkommen!")
        Button(onClick = { scope.launch { onLogout() } }) {
            Text("Logout")
        }
    }
}
