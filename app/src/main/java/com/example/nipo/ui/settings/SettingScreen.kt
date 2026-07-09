package com.example.nipo.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AuthRepository
import com.example.nipo.ui.common.AppTopBar
import com.example.nipo.ui.common.NipoTextField
import com.example.nipo.ui.common.PrimaryButton
import com.example.nipo.ui.common.SecondaryButton
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
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

    Column(
        Modifier
            .fillMaxSize()
            .background(NeutralBg)
    ) {
        AppTopBar(title = "設定")

        Column(Modifier.padding(20.dp)) {
            if (!isLoading) {
                Text("ニックネーム", style = MaterialTheme.typography.labelMedium, color = NeutralMutedText)
                Spacer(Modifier.height(8.dp))
                NipoTextField(value = displayName, onValueChange = { displayName = it }, placeholder = "ニックネーム")
                Spacer(Modifier.height(12.dp))
                PrimaryButton(
                    text = "保存",
                    onClick = {
                        scope.launch {
                            authRepository.updateDisplayName(displayName)
                                .onSuccess { saveMessage = "保存しました" }
                                .onFailure { saveMessage = "エラー: ${it.message}" }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                saveMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.bodySmall, color = NeutralMutedText)
                }
            }

            Spacer(Modifier.height(32.dp))

            SecondaryButton(
                text = "ログアウト",
                onClick = {
                    authRepository.signOut()
                    onLoggedOut()
                },
            )
        }
    }
}
