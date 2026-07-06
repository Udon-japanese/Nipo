package com.example.nipo.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AuthRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onLoggedOut: () -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    var displayName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var saveMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        displayName = authRepository.getDisplayName()
        isLoading = false
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("設定", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        if (!isLoading) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("表示名") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                scope.launch {
                    authRepository.updateDisplayName(displayName)
                        .onSuccess { saveMessage = "保存しました" }
                        .onFailure { saveMessage = "エラー: ${it.message}" }
                }
            }) { Text("保存") }
            saveMessage?.let { Text(it, modifier = Modifier.padding(top = 8.dp)) }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = {
                authRepository.signOut()
                onLoggedOut()
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("ログアウト") }
    }
}