package com.example.nipo.ui.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.data.AuthRepository
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import com.example.nipo.data.PostTag
import com.example.nipo.data.SosPost
import com.example.nipo.data.SosRepository
import com.example.nipo.data.SosStatus
import com.example.nipo.ui.theme.rememberNotchedCardShape
import com.example.nipo.ui.theme.NeutralAccent
import com.example.nipo.ui.theme.NeutralBg
import com.example.nipo.ui.theme.NeutralMutedText
import com.example.nipo.ui.theme.NeutralText
import com.example.nipo.ui.theme.SosBg
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.TipsAccent
import com.example.nipo.ui.theme.TipsBg
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MyPageScreen(
    onOpenSettings: () -> Unit,
    onLoggedOut: () -> Unit,
    onOpenPost: (String) -> Unit,
    onOpenSos: (String) -> Unit,
) {
    val context = LocalContext.current
    val postRepository = remember { PostRepository(context) }
    val sosRepository = remember { SosRepository(context) }
    val authRepository = remember { AuthRepository(context) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var nickname by remember { mutableStateOf("ゲスト") }
    var myTips by remember { mutableStateOf<List<Post>>(emptyList()) }
    var mySos by remember { mutableStateOf<List<SosPost>>(emptyList()) }

    LaunchedEffect(Unit) {
        nickname = authRepository.getDisplayName()
    }
    LaunchedEffect(currentUid) {
        postRepository.observePosts().collectLatest { all ->
            myTips = all.filter { it.authorUid == currentUid }
        }
    }
    LaunchedEffect(currentUid) {
        sosRepository.observeMySos(currentUid).collectLatest { mySos = it }
    }

    val totalGood = myTips.sumOf { it.goodCount }
    val avatarInitial = nickname.firstOrNull()?.toString() ?: "？"

    Column(Modifier.fillMaxSize().background(NeutralBg)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "マイページ",
                style = MaterialTheme.typography.titleLarge,
                color = NeutralText,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "設定", tint = NeutralText)
            }
        }

        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            color = Color.White,
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(NeutralAccent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(avatarInitial, color = Color(0xFFF3F2EE), style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(nickname, style = MaterialTheme.typography.titleMedium, color = NeutralText)
                        Text("Nipo メンバー", style = MaterialTheme.typography.labelSmall, color = NeutralMutedText)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        bg = Color(0xFFF8F4EC),
                        valueColor = TipsAccent,
                        value = myTips.size.toString(),
                        label = "残した置き手紙",
                    )
                    Spacer(Modifier.width(10.dp))
                    StatCard(
                        modifier = Modifier.weight(1f),
                        bg = SosBg,
                        valueColor = SosGradientEnd,
                        value = mySos.size.toString(),
                        label = "伝えた困りごと",
                    )
                    Spacer(Modifier.width(10.dp))
                    StatCard(
                        modifier = Modifier.weight(1f),
                        bg = Color(0xFFF3F2EE),
                        valueColor = NeutralText,
                        value = totalGood.toString(),
                        label = "受け取った反応",
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
        ) {
            item {
                Spacer(Modifier.height(22.dp))
                SectionLabel(color = TipsAccent, text = "残した置き手紙")
                Spacer(Modifier.height(10.dp))
                if (myTips.isEmpty()) {
                    Text("まだ投稿がありません", style = MaterialTheme.typography.bodySmall, color = NeutralMutedText)
                } else {
                    TipsGrid(myTips, onClick = { onOpenPost(it) })
                }

                Spacer(Modifier.height(22.dp))
                SectionLabel(color = SosGradientEnd, text = "伝えた困りごと")
                Spacer(Modifier.height(10.dp))
            }
            items(mySos) { sos ->
                SosHistoryRow(sos, onClick = { onOpenSos(sos.id) })
                Spacer(Modifier.height(8.dp))
            }
            item {
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = {
                        authRepository.signOut()
                        onLoggedOut()
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LogoutIcon(tint = Color(0xFFA05A4A))
                    Spacer(Modifier.width(8.dp))
                    Text("ログアウト", color = Color(0xFFA05A4A))
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, bg: Color, valueColor: Color, value: String, label: String) {
    Surface(modifier = modifier, color = bg, shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(value, style = MaterialTheme.typography.titleMedium, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = valueColor.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun SectionLabel(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(7.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = NeutralMutedText)
    }
}

@Composable
private fun TipsGrid(tips: List<Post>, onClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        tips.chunked(3).forEach { rowTips ->
            Row(modifier = Modifier.fillMaxWidth()) {
                rowTips.forEach { tip ->
                    TipsGridCell(tip, modifier = Modifier.weight(1f), onClick = { onClick(tip.id) })
                }
                repeat(3 - rowTips.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TipsGridCell(tip: Post, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val tag = runCatching { PostTag.valueOf(tip.label) }.getOrNull()
    val label = tag?.displayName ?: tip.title
    val photoUrl = tip.photoUrls.firstOrNull()
    val shape = rememberNotchedCardShape(8.dp)

    Box(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(shape)
            .background(if (photoUrl == null) TipsBg.copy(alpha = 0.4f) else Color(0xFFE6DBC0))
            .clickable(onClick = onClick),
        contentAlignment = if (photoUrl == null) Alignment.Center else Alignment.BottomStart,
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xCC26241F)),
                            startY = 40f,
                        )
                    )
            )
        }
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (photoUrl == null) TipsAccent else Color.White,
            textAlign = if (photoUrl == null) TextAlign.Center else TextAlign.Start,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(8.dp),
        )
    }
}

@Composable
private fun SosHistoryRow(sos: SosPost, onClick: () -> Unit) {
    val isOpen = sos.status == SosStatus.OPEN.name
    val (bg, fg, label) = if (isOpen) Triple(Color(0xFFFBE9D0), Color(0xFFA66A12), "対応中")
    else Triple(Color(0xFFEEEEEE), Color(0xFF888888), "解決済み")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(sos.title.ifBlank { sos.category }, style = MaterialTheme.typography.bodySmall, color = NeutralText)
        Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
            Text(label, color = fg, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
        }
    }
}

@Composable
private fun LogoutIcon(tint: Color) {
    Canvas(modifier = Modifier.size(15.dp)) {
        val strokeWidth = size.width * 0.12f
        val doorWidth = size.width * 0.55f
        drawRoundRect(
            color = tint,
            topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(doorWidth, size.height),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.width * 0.1f),
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(doorWidth * 0.7f, size.height / 2f),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        val arrowSize = size.width * 0.18f
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(size.width - arrowSize, size.height / 2f - arrowSize),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(size.width - arrowSize, size.height / 2f + arrowSize),
            end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2f),
            strokeWidth = strokeWidth,
            cap = androidx.compose.ui.graphics.StrokeCap.Round,
        )
    }
}
