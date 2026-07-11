package com.example.nipo.ui.sosdetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.nipo.data.SosCategory
import com.example.nipo.data.SosMessage
import com.example.nipo.data.SosPost
import com.example.nipo.data.SosRepository
import com.example.nipo.data.SosStatus
import com.example.nipo.ui.common.NipoTextField
import com.example.nipo.ui.common.SosGradientHeader
import com.example.nipo.ui.common.formatRelativeTime
import com.example.nipo.ui.theme.NipoMode
import com.example.nipo.ui.theme.NipoModeProvider
import com.example.nipo.ui.theme.SosBg
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.SosGradientStart
import com.example.nipo.ui.theme.sosHeaderGradient
import com.example.nipo.ui.theme.style
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ステータスは投稿者が「解決済みにする」を押したときのみ変化する(自動クローズは行わない)
private fun effectiveStatus(post: SosPost): SosStatus =
    if (post.status == SosStatus.CLOSED.name) SosStatus.CLOSED else SosStatus.OPEN

@Composable
fun SosDetailScreen(
    sosId: String,
    onBack: (SosPost?) -> Unit,
    onEdit: () -> Unit = {},
    onDeleted: () -> Unit = {},
) {
    val context = LocalContext.current
    val repository = remember { SosRepository(context) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val scope = rememberCoroutineScope()

    var post by remember { mutableStateOf<SosPost?>(null) }
    var messages by remember { mutableStateOf<List<SosMessage>>(emptyList()) }
    var chatDraft by remember { mutableStateOf("") }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    LaunchedEffect(sosId) {
        post = repository.getSos(sosId)
    }
    LaunchedEffect(sosId) {
        repository.observeSosMessages(sosId).collectLatest { messages = it }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    var showReportStub by remember { mutableStateOf(false) }

    NipoModeProvider(NipoMode.Sos) {
        Column(Modifier.fillMaxSize().background(SosBg)) {
            val category = post?.let { runCatching { SosCategory.valueOf(it.category) }.getOrNull() }
            Box(modifier = Modifier.fillMaxWidth()) {
                SosGradientHeader(
                    title = post?.title?.takeIf { it.isNotBlank() } ?: "SOS",
                    onBack = { onBack(post) },
                    showSecondCircle = false,
                ) {
                    Text(
                        "通報",
                        color = Color.White.copy(alpha = 0.85f),
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { showReportStub = true },
                    )
                }

                post?.let { p ->
                    val status = effectiveStatus(p)
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(start = 14.dp, end = 14.dp, top = 75.dp)
                            .fillMaxWidth()
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("差出人：匿名ユーザー", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = SosGradientStart)
                            Text(
                                "日時：${formatRelativeTime(p.createdAt)}",
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.labelSmall,
                                color = SosGradientStart,
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End) {
                                val (bg, fg, label) = if (status == SosStatus.OPEN) {
                                    Triple(SosBg, SosGradientEnd, "対応中")
                                } else {
                                    Triple(Color(0xFFE5E5E5), Color(0xFF6B6255), "解決済み")
                                }
                                Surface(color = bg, shape = RoundedCornerShape(6.dp)) {
                                    Text(label, color = fg, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                                }
                            }
                            category?.let {
                                Spacer(Modifier.height(8.dp))
                                Surface(color = it.style.bg, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        it.label,
                                        color = it.style.fg,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(p.text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2B2620))
                            if (p.authorUid == currentUid) {
                                Spacer(Modifier.height(12.dp))
                                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                                    OutlinedButton(
                                        onClick = onEdit,
                                        modifier = Modifier.weight(1f),
                                    ) { Text("編集する", color = SosGradientStart) }
                                    Button(
                                        onClick = { showDeleteConfirm = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        modifier = Modifier.weight(1f),
                                    ) { Text("削除する", color = Color.White) }
                                }
                            }
                        }
                    }
                }
            }

            post?.let { p ->
                val status = effectiveStatus(p)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp),
                ) {
                    items(messages) { message ->
                        ChatBubble(message = message, isOwnMessage = message.senderUid == currentUid)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .border(BorderStroke(1.dp, Color(0xFFDCE8F2)))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NipoTextField(
                        value = chatDraft,
                        onValueChange = { chatDraft = it },
                        placeholder = "メッセージを入力",
                        modifier = Modifier.weight(1f),
                        containerColor = Color(0xFFF5F9FC),
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(sosHeaderGradient(), RoundedCornerShape(10.dp))
                            .clickable(enabled = chatDraft.isNotBlank()) {
                                val toSend = chatDraft
                                chatDraft = ""
                                scope.launch {
                                    repository.sendSosMessage(sosId, SosMessage(senderUid = currentUid, text = toSend))
                                }
                            }
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Text("送信", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    }
                }

                if (p.authorUid == currentUid) {
                    Box(Modifier.background(Color.White).padding(horizontal = 14.dp, vertical = 12.dp)) {
                        if (status == SosStatus.OPEN) {
                            Surface(
                                color = Color(0xFFEAF1F8),
                                border = BorderStroke(1.dp, SosGradientStart),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            repository.closeSos(sosId)
                                            post = post?.copy(status = SosStatus.CLOSED.name, closedAt = Timestamp.now())
                                        }
                                    },
                            ) {
                                Text(
                                    "解決済みにする",
                                    color = SosGradientEnd,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        } else {
                            Surface(
                                color = Color(0xFFF3F2EE),
                                border = BorderStroke(1.dp, Color(0xFFB8B2A4)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        scope.launch {
                                            repository.reopenSos(sosId)
                                            post = post?.copy(status = SosStatus.OPEN.name, closedAt = null)
                                        }
                                    },
                            ) {
                                Text(
                                    "対応中に戻す",
                                    color = Color(0xFF6B6255),
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showReportStub) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { showReportStub = false },
                title = { Text("通報") },
                text = { Text("通報機能は準備中です。") },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = { showReportStub = false }) { Text("閉じる") }
                },
            )
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("この困りごとを削除しますか？") },
                text = { Text("削除すると元に戻せません。") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            repository.deleteSos(sosId)
                            onDeleted()
                        }
                    }) { Text("削除する", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) { Text("キャンセル") }
                },
            )
        }
    }
}
