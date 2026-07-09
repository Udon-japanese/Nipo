package com.example.nipo.ui.postcreate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import com.example.nipo.data.PostTag
import com.example.nipo.ui.common.AppTopBar
import com.example.nipo.ui.common.NipoTextField
import com.example.nipo.ui.common.PhotoSlotGrid
import com.example.nipo.ui.common.PrimaryButton
import com.example.nipo.ui.common.TagChipGroup
import com.example.nipo.ui.theme.NipoMode
import com.example.nipo.ui.theme.NipoModeProvider
import com.example.nipo.ui.theme.TipsBg
import com.example.nipo.ui.theme.TipsBorder
import com.example.nipo.ui.theme.TipsCard
import com.example.nipo.ui.theme.TipsMutedText
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

@Composable
fun CreatePostScreen(
    onDone: () -> Unit,
    onPickLocation: () -> Unit,
    navController: NavHostController,
    editingPostId: String? = null,
) {
    val context = LocalContext.current
    val repository = remember { PostRepository(context) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isEditMode = editingPostId != null

    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<PostTag?>(null) }
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var placeName by remember { mutableStateOf<String?>(null) }
    var existingPhotoUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var existingCreatedAt by remember { mutableStateOf<com.google.firebase.Timestamp?>(null) }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isPosting by remember { mutableStateOf(false) }
    var isSealing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(editingPostId) {
        if (editingPostId != null) {
            repository.getPost(editingPostId)?.let { existing ->
                title = existing.title
                detail = existing.locationDetail
                selectedTag = runCatching { PostTag.valueOf(existing.label) }.getOrNull()
                placeName = existing.placeName
                existing.geo?.let { pickedLatLng = LatLng(it.latitude, it.longitude) }
                existingPhotoUrls = existing.photoUrls
                existingCreatedAt = existing.createdAt
            }
        }
    }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<Double?>("pickedLat", null)?.collect { lat ->
            if (lat == null) return@collect
            val lng = savedStateHandle.get<Double>("pickedLng") ?: return@collect
            pickedLatLng = LatLng(lat, lng)
            placeName = savedStateHandle.get<String>("pickedPlaceName")
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { if (imageUris.size < 3) imageUris = imageUris + it } }

    NipoModeProvider(NipoMode.Tips) {
        Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TipsBg)
                .verticalScroll(rememberScrollState())
        ) {
            AppTopBar(title = if (isEditMode) "置き手紙を編集" else "置き手紙", onBack = onDone)

            Surface(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .rotate(-0.4f)
                    .shadow(elevation = 10.dp, shape = RoundedCornerShape(3.dp)),
                color = TipsCard,
                border = BorderStroke(1.dp, TipsBorder),
                shape = RoundedCornerShape(3.dp),
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text("場所", style = MaterialTheme.typography.labelMedium, color = TipsMutedText)
                    Spacer(Modifier.height(8.dp))
                    PrimaryButton(
                        text = if (pickedLatLng == null) "地図で場所を選ぶ" else "場所選択済み ✓ ${placeName.orEmpty()}",
                        onClick = onPickLocation,
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("タイトル", style = MaterialTheme.typography.labelMedium, color = TipsMutedText)
                    Spacer(Modifier.height(8.dp))
                    NipoTextField(value = title, onValueChange = { title = it }, placeholder = "例：3階トイレの前")

                    Spacer(Modifier.height(16.dp))
                    Text("タグを選択（任意）", style = MaterialTheme.typography.labelMedium, color = TipsMutedText)
                    Spacer(Modifier.height(8.dp))
                    TagChipGroup(
                        options = PostTag.entries,
                        selected = setOfNotNull(selectedTag),
                        onToggle = { selectedTag = it },
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("写真（最大3枚）", style = MaterialTheme.typography.labelMedium, color = TipsMutedText)
                    if (isEditMode && imageUris.isEmpty() && existingPhotoUrls.isNotEmpty()) {
                        Text(
                            "新しい写真を選ぶと、既存の写真と差し替わります",
                            style = MaterialTheme.typography.bodySmall,
                            color = TipsMutedText,
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    PhotoSlotGrid(
                        photoUris = imageUris,
                        maxSlots = 3,
                        onAddPhoto = { imagePicker.launch("image/*") },
                        onRemovePhoto = { index -> imageUris = imageUris.toMutableList().apply { removeAt(index) } },
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("本文", style = MaterialTheme.typography.labelMedium, color = TipsMutedText)
                    Spacer(Modifier.height(8.dp))
                    NipoTextField(value = detail, onValueChange = { if (it.length <= 200) detail = it }, placeholder = "どんな情報を残しますか？", singleLine = false)
                }
            }

            Column(Modifier.padding(horizontal = 14.dp)) {
                Spacer(Modifier.height(10.dp))
                PrimaryButton(
                    text = if (isEditMode) "更新" else "ポストに投函",
                    loading = isPosting,
                    enabled = title.isNotBlank() && pickedLatLng != null && !isPosting,
                    onClick = {
                        isPosting = true
                        val geo = pickedLatLng?.let { GeoPoint(it.latitude, it.longitude) }
                        val post = Post(
                            title = title,
                            label = selectedTag?.name ?: "",
                            locationDetail = detail,
                            geo = geo,
                            placeName = placeName,
                            authorUid = currentUid,
                            photoUrls = existingPhotoUrls,
                            createdAt = existingCreatedAt,
                        )
                        scope.launch {
                            if (editingPostId != null) {
                                repository.updatePost(editingPostId, post, imageUris)
                                isPosting = false
                                onDone()
                            } else {
                                repository.createPost(post, imageUris)
                                isPosting = false
                                isSealing = true
                            }
                        }
                    },
                )
                Spacer(Modifier.height(24.dp))
            }
        }

        TipsSealOverlay(visible = isSealing, onFinished = onDone)
        }
    }
}
