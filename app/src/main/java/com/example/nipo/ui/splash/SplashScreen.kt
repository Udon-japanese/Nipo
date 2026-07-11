package com.example.nipo.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nipo.data.AppPreferences
import com.example.nipo.data.AuthRepository
import com.example.nipo.ui.theme.NeutralAccent
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.NeutralOnAccent

sealed interface SplashDestination {
    data object LocationPermission : SplashDestination
    data object Login : SplashDestination
    data object Nickname : SplashDestination
    data object Home : SplashDestination
}

@Composable
fun SplashScreen(onResolved: (SplashDestination) -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val appPreferences = remember { AppPreferences(context) }

    LaunchedEffect(Unit) {
        val destination = when {
            !appPreferences.hasAskedLocationPermission() -> SplashDestination.LocationPermission
            authRepository.currentUser == null -> SplashDestination.Login
            authRepository.getDisplayName() == "匿名" -> SplashDestination.Nickname
            else -> SplashDestination.Home
        }
        onResolved(destination)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NeutralBg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(NeutralAccent),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(NeutralOnAccent)
            )
        }
        androidx.compose.foundation.layout.Spacer(Modifier.size(18.dp))
        Text("Nipo", style = MaterialTheme.typography.displayLarge, color = NeutralAccent)
        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
        Text(
            "過去の誰かの優しさが、時を超えて\n今の誰かを助ける位置情報SNS",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralMutedText,
            textAlign = TextAlign.Center,
        )
    }
}
