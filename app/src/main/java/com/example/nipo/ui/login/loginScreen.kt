package com.example.nipo.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AuthRepository
import kotlinx.coroutines.launch

private const val WEB_CLIENT_ID = "858388256208-i605b7rp10mc599v0fpmtvpuur9l4uk4.apps.googleusercontent.com"

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("投稿にはログインが必要です")
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                scope.launch {
                    authRepository.signInWithGoogle(WEB_CLIENT_ID)
                        .onSuccess { onLoginSuccess() }
                        .onFailure { errorMessage = it.message }
                }
            }) {
                Text("Googleでログイン")
            }
            errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text("エラー: $it")
            }
        }
    }
}