package com.example.nipo.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AuthRepository
import com.example.nipo.ui.common.NipoTextField
import com.example.nipo.ui.common.PrimaryButton
import com.example.nipo.ui.theme.NeutralAccent
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import kotlinx.coroutines.launch

@Composable
fun NicknameScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val scope = rememberCoroutineScope()
    var nickname by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(32.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        Text("ニックネームを設定", style = MaterialTheme.typography.titleLarge, color = NeutralAccent)
        Spacer(Modifier.height(12.dp))
        Text(
            "本名は使用しません。匿名で参加できます。あとから変更できます。",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMutedText,
        )
        Spacer(Modifier.height(16.dp))
        NipoTextField(value = nickname, onValueChange = { nickname = it }, placeholder = "例：まちのねこ")
        Spacer(Modifier.height(20.dp))
        PrimaryButton(
            text = "はじめる",
            loading = isSaving,
            enabled = nickname.isNotBlank(),
            onClick = {
                isSaving = true
                scope.launch {
                    authRepository.updateDisplayName(nickname)
                    isSaving = false
                    onFinished()
                }
            },
        )
    }
}
