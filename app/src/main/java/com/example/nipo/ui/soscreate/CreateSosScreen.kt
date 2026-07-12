package com.example.nipo.ui.soscreate

import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.example.nipo.data.GeoUtils
import com.example.nipo.data.SosCategory
import com.example.nipo.data.SosPost
import com.example.nipo.data.SosRepository
import com.example.nipo.data.SosStatus
import com.example.nipo.ui.common.NipoTextField
import com.example.nipo.ui.common.PhotoSlotGrid
import com.example.nipo.ui.common.SimpleFlowRow
import com.example.nipo.ui.common.SosGradientHeader
import com.example.nipo.ui.sosdetail.EmergencyBanner
import com.example.nipo.ui.theme.NipoMode
import com.example.nipo.ui.theme.NipoModeProvider
import com.example.nipo.ui.theme.SosBg
import com.example.nipo.ui.theme.SosGradientEnd
import com.example.nipo.ui.theme.SosGradientStart
import com.example.nipo.ui.theme.sosHeaderGradient
import com.example.nipo.ui.theme.sosInfoGradient
import com.example.nipo.ui.theme.style
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun CreateSosScreen(onDone: () -> Unit, editingSosId: String? = null) {
    val context = LocalContext.current
    val repository = remember { SosRepository(context) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    val isEditing = editingSosId != null

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<SosCategory?>(null) }
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPost by remember { mutableStateOf<SosPost?>(null) }
    var isPosting by remember { mutableStateOf(false) }
    var isShowingAnimation by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(editingSosId) {
        if (editingSosId != null) {
            repository.getSos(editingSosId)?.let { post ->
                existingPost = post
                title = post.title
                selectedCategory = runCatching { SosCategory.valueOf(post.category) }.getOrNull()
                text = post.text
            }
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    NipoModeProvider(NipoMode.Sos) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SosBg)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SosGradientHeader(title = if (isEditing) "メッセージを編集" else "新規メッセージ", onBack = onDone)

                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(start = 14.dp, end = 14.dp, top = 80.dp)
                            .fillMaxWidth()
                            .shadow(elevation = 12.dp, shape = RoundedCornerShape(14.dp)),
                        color = Color.White,
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("宛先", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = SosGradientStart, modifier = Modifier.width(52.dp))
                                Text("半径500m以内にいる人", style = MaterialTheme.typography.bodySmall, color = SosGradientEnd)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("件名", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = SosGradientStart, modifier = Modifier.width(52.dp))
                                NipoTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    placeholder = "例：駅前で迷っています",
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }

                Column(Modifier.padding(16.dp)) {
                    if (selectedCategory?.showsEmergencyBanner == true) {
                        EmergencyBanner()
                        Spacer(Modifier.height(16.dp))
                    }

                    Text("タグ（任意）", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = SosGradientStart)
                    Spacer(Modifier.height(8.dp))
                    SimpleFlowRow(horizontalGap = 7.dp, verticalGap = 7.dp) {
                        SosCategory.entries.forEach { category ->
                            SosTagChip(
                                category = category,
                                selected = category == selectedCategory,
                                onClick = { selectedCategory = if (selectedCategory == category) null else category },
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Surface(color = Color.White, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("本文", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = SosGradientStart)
                            Spacer(Modifier.height(8.dp))
                            NipoTextField(
                                value = text,
                                onValueChange = { text = it },
                                placeholder = "今どんな状況ですか？気軽に書いてください。",
                                singleLine = false,
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("添付（任意・1枚）", fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, color = SosGradientStart)
                    if (isEditing && imageUri == null && existingPost?.photoUrl != null) {
                        Text(
                            "新しい写真を選ぶと、既存の写真と差し替わります",
                            style = MaterialTheme.typography.bodySmall,
                            color = SosGradientStart,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    PhotoSlotGrid(
                        photoUris = listOfNotNull(imageUri),
                        maxSlots = 1,
                        onAddPhoto = { imagePicker.launch("image/*") },
                        onRemovePhoto = { imageUri = null },
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(sosInfoGradient(), RoundedCornerShape(10.dp))
                            .border(BorderStroke(1.dp, Color(0xFFCFE0F0)), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = SosGradientEnd)
                        Text(
                            "半径500m以内の人に届きます（変更不可）",
                            color = SosGradientEnd,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    val canSubmit = title.isNotBlank() && text.isNotBlank() && !isPosting
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = if (canSubmit) sosHeaderGradient() else Brush.linearGradient(listOf(Color(0xFFB9C6D4), Color(0xFFB9C6D4))),
                                shape = RoundedCornerShape(10.dp),
                            )
                            .clickable(enabled = canSubmit) {
                                isPosting = true
                                scope.launch {
                                    if (isEditing) {
                                        val existing = existingPost
                                        val result = repository.updateSos(
                                            editingSosId!!,
                                            SosPost(
                                                authorUid = existing?.authorUid ?: currentUid,
                                                location = existing?.location,
                                                title = title,
                                                category = selectedCategory?.name ?: "",
                                                text = text,
                                                photoUrl = existing?.photoUrl,
                                                status = existing?.status ?: SosStatus.OPEN.name,
                                                createdAt = existing?.createdAt,
                                                closedAt = existing?.closedAt,
                                            ),
                                            imageUri,
                                        )
                                        isPosting = false
                                        result.onSuccess {
                                            onDone()
                                        }.onFailure {
                                            errorMessage = it.message
                                        }
                                    } else {
                                        val location = try {
                                            fusedClient.lastLocation.await()
                                        } catch (_: Exception) {
                                            null
                                        }
                                        if (location == null) {
                                            isPosting = false
                                            return@launch
                                        }
                                        val geo = GeoUtils.coarsen(location.latitude, location.longitude)
                                        val result = repository.createSos(
                                            SosPost(
                                                authorUid = currentUid,
                                                location = geo,
                                                title = title,
                                                category = selectedCategory?.name ?: "",
                                                text = text,
                                            ),
                                            imageUri,
                                        )
                                        isPosting = false
                                        result.onSuccess {
                                            isShowingAnimation = true
                                        }.onFailure {
                                            errorMessage = it.message
                                        }
                                    }
                                }
                            }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text(if (isEditing) "更新する" else "SOSを送る", color = Color.White, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text("送信エラー: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            SosEmailOverlay(
                visible = isShowingAnimation,
                onFinished = onDone
            )
        }
    }
}

@Composable
private fun SosTagChip(category: SosCategory, selected: Boolean, onClick: () -> Unit) {
    val style = category.style
    Surface(
        modifier = Modifier
            .padding(end = 7.dp, bottom = 7.dp)
            .clickable(onClick = onClick),
        color = if (selected) style.bg else Color.White,
        contentColor = if (selected) style.fg else style.border,
        border = BorderStroke(if (selected) 0.dp else 1.dp, style.border),
        shape = RoundedCornerShape(6.dp),
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) {
            Text(category.label, style = MaterialTheme.typography.labelSmall)
        }
    }
}
