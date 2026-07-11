package com.example.nipo.ui.permission

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AppPreferences
import com.example.nipo.ui.common.PrimaryButton
import com.example.nipo.ui.theme.NeutralAccent
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.TipsPlaceholderBg
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val appPreferences = androidx.compose.runtime.remember { AppPreferences(context) }
    val scope = rememberCoroutineScope()
    val permissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    fun finish() {
        scope.launch {
            appPreferences.setAskedLocationPermission()
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(TipsPlaceholderBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.LocationOn, contentDescription = null, tint = NeutralAccent, modifier = Modifier.size(36.dp))
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(20.dp))
        Text("現在地を使用しますか？", style = MaterialTheme.typography.titleLarge, color = NeutralAccent)
        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        Text(
            "近くの置き手紙やSOSを表示するために、位置情報を使用します。正確な位置が他のユーザーに公開されることはありません。",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMutedText,
            textAlign = TextAlign.Center,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.size(24.dp))
        PrimaryButton(text = "許可する", onClick = {
            permissionState.launchPermissionRequest()
            finish()
        })
        TextButton(onClick = { finish() }) {
            Text("あとで設定する", color = NeutralMutedText)
        }
    }
}
