package com.example.nipo.ui.postdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.nipo.data.Comment
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import com.example.nipo.data.PostTag
import com.example.nipo.ui.common.AppTopBar
import com.example.nipo.ui.common.LetterCard
import com.example.nipo.ui.common.NipoTextField
import com.example.nipo.ui.common.PostPhotoCarousel
import com.example.nipo.ui.common.TagChip
import com.example.nipo.ui.common.WaxSeal
import com.example.nipo.ui.common.formatRelativeTime
import com.example.nipo.ui.theme.NeutralText
import com.example.nipo.ui.theme.NipoMode
import com.example.nipo.ui.theme.NipoModeProvider
import com.example.nipo.ui.theme.TipsAccent
import com.example.nipo.ui.theme.TipsBg
import com.example.nipo.ui.theme.TipsMutedText
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDeleted: () -> Unit,
) {
    val context = LocalContext.current
    val repository = remember { PostRepository(context) }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var post by remember { mutableStateOf<Post?>(null) }
    var sendError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        post = repository.getPost(postId)
    }

    LaunchedEffect(postId) {
        repository.observeComments(postId).collectLatest { comments = it }
    }

    NipoModeProvider(NipoMode.Tips) {
        Column(
            Modifier
                .fillMaxSize()
                .background(TipsBg)
        ) {
            AppTopBar(title = "", onBack = onBack)
            post?.let { p ->
                val tag = runCatching { PostTag.valueOf(p.label) }.getOrNull()
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                    LetterCard {
                        PostPhotoCarousel(photoUrls = p.photoUrls)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            tag?.let { TagChip(tag = it) }
                            if (p.isPossiblyOutdated) OutdatedBanner()
                        }
                        Spacer(Modifier.height(8.dp))
                        p.placeName?.let {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = TipsAccent)
                            Spacer(Modifier.height(4.dp))
                        }
                        Text(p.title, style = MaterialTheme.typography.titleMedium, color = TipsAccent)
                        Spacer(Modifier.height(8.dp))
                        Text(p.locationDetail, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "— ${formatRelativeTime(p.createdAt)}に残された手紙",
                            style = MaterialTheme.typography.labelSmall,
                            color = TipsMutedText,
                        )
                        Spacer(Modifier.height(12.dp))
                        p.geo?.let { geo ->
                            val latLng = LatLng(geo.latitude, geo.longitude)
                            val previewCameraState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(latLng, 18f)
                            }
                            GoogleMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                cameraPositionState = previewCameraState,
                                uiSettings = MapUiSettings(
                                    zoomControlsEnabled = true,
                                    scrollGesturesEnabled = true,
                                    zoomGesturesEnabled = true,
                                    tiltGesturesEnabled = false,
                                    rotationGesturesEnabled = false
                                )
                            ) {
                                Marker(state = rememberMarkerState(position = latLng))
                            }
                            Spacer(Modifier.height(14.dp))
                        }
                        DashedDivider()
                        Spacer(Modifier.height(14.dp))
                        ReactionRow(
                            goodCount = p.goodCount,
                            badCount = p.badCount,
                            onReactGood = {
                                post = p.copy(goodCount = p.goodCount + 1)
                                scope.launch { repository.reactGood(postId) }
                            },
                            onReactBad = {
                                post = p.copy(badCount = p.badCount + 1)
                                scope.launch { repository.reactBad(postId) }
                            },
                        )
                        if (p.authorUid == currentUid) {
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = onEdit,
                                    modifier = Modifier.weight(1f),
                                ) { Text("編集する", color = TipsAccent) }
                                Button(
                                    onClick = { showDeleteConfirm = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f),
                                ) { Text("削除する", color = Color.White) }
                            }
                        }
                    }
                    WaxSeal(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-22).dp, y = (-16).dp)
                            .rotate(-8f),
                        size = 46.dp,
                    )
                }
            }
            Text(
                "コメント",
                style = MaterialTheme.typography.labelLarge,
                color = TipsAccent,
                modifier = Modifier.padding(horizontal = 14.dp),
            )
            Spacer(Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp),
            ) {
                items(comments) { comment ->
                    Surface(
                        color = Color(0xFFF5EDD8),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                    ) {
                        Text(
                            buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("匿名") }
                                append("　${comment.text}")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = NeutralText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NipoTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "コメントを追加",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            val result = repository.addComment(postId, Comment(text = text, authorUid = currentUid), null)
                            result.onSuccess {
                                text = ""
                                sendError = null
                            }.onFailure {
                                sendError = it.message
                            }
                        }
                    },
                    enabled = text.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TipsAccent),
                ) { Text("送信", color = Color.White) }
            }
            sendError?.let {
                Text(
                    "送信エラー: $it",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 14.dp),
                )
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("この投稿を削除しますか？") },
                text = { Text("削除すると元に戻せません。") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        scope.launch {
                            repository.deletePost(postId)
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
