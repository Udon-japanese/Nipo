package com.example.nipo.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AuthRepository
import com.example.nipo.ui.common.AppTopBar
import com.example.nipo.ui.common.NipoSwitch
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.NeutralText
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit, onLoggedOut: () -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    var displayName by remember { mutableStateOf("") }
    var notifSos by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        displayName = authRepository.getDisplayName()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(NeutralBg)
            .verticalScroll(rememberScrollState())
    ) {
        AppTopBar(title = "設定", onBack = onBack)

        Column(Modifier.padding(horizontal = 20.dp)) {
            SectionHeader("通知")
            ToggleSettingRow(
                label = "困りごと通知",
                checked = notifSos,
                onCheckedChange = { notifSos = it },
            )
            Spacer(Modifier.height(8.dp))
            SettingRow(label = "通知半径の希望") {
                Text("500m", style = MaterialTheme.typography.bodySmall, color = NeutralMutedText)
            }

            SectionHeader("アカウント", topPadding = 22.dp)
            SettingRow(label = "ニックネーム") {
                BasicTextField(
                    value = displayName,
                    onValueChange = { name ->
                        displayName = name
                        if (name.isNotBlank()) {
                            scope.launch { authRepository.updateDisplayName(name) }
                        }
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.End, color = NeutralMutedText),
                    modifier = Modifier.width(140.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            StaticRow(label = "ブロックリスト管理")

            SectionHeader("その他", topPadding = 22.dp)
            StaticRow(label = "利用規約")
            Spacer(Modifier.height(8.dp))
            StaticRow(label = "プライバシーポリシー")
            Spacer(Modifier.height(8.dp))
            StaticRow(label = "アカウントを削除", labelColor = Color(0xFFC0392B))

            Spacer(Modifier.height(24.dp))
            StaticRow(label = "ログアウト", labelColor = Color(0xFFA05A4A), onClick = {
                authRepository.signOut()
                onLoggedOut()
            })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionHeader(text: String, topPadding: androidx.compose.ui.unit.Dp = 8.dp) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = NeutralMutedText,
        modifier = Modifier.padding(top = topPadding, bottom = 6.dp),
    )
}

@Composable
private fun ToggleSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = NeutralText, modifier = Modifier.weight(1f))
        NipoSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = NeutralText, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun StaticRow(label: String, labelColor: Color = NeutralText, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(10.dp))
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = labelColor)
    }
}
