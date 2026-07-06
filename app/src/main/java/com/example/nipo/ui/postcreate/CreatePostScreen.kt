package com.example.nipo.ui.postcreate

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.data.Post
import com.example.nipo.data.PostLabel
import com.example.nipo.data.PostRepository
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.launch

private val TroubledAccent = Color(0xFFE53935)
private val TroubledBg = Color(0xFFFFEBEE)
private val LetterAccent = Color(0xFF1E88E5)
private val LetterBg = Color(0xFFE3F2FD)

@Composable
fun CreatePostScreen(onDone: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { PostRepository(context) }
    val placesClient = remember { Places.createClient(context) }
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var title by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var label by remember { mutableStateOf(PostLabel.TROUBLED) }
    var pickedLatLng by remember { mutableStateOf<LatLng?>(null) }
    var placeName by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showMap by remember { mutableStateOf(false) }
    var isPosting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    if (showMap) {
        MapPickerScreen(
            placesClient = placesClient,
            onLocationPicked = { latLng, name ->
                pickedLatLng = latLng; placeName = name; showMap = false
            },
            onCancel = { showMap = false }
        )
        return
    }

    val accentColor by animateColorAsState(
        targetValue = if (label == PostLabel.TROUBLED) TroubledAccent else LetterAccent,
        label = "accentColor"
    )
    val bgColor by animateColorAsState(
        targetValue = if (label == PostLabel.TROUBLED) TroubledBg else LetterBg,
        label = "bgColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(rememberScrollState())
    ) {
        // ★ 画面上部の帯 — 今どちらを投稿しようとしているか一目で分かる
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(accentColor)
                .padding(vertical = 20.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (label == PostLabel.TROUBLED) Icons.Default.Warning else Icons.Default.Favorite,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = if (label == PostLabel.TROUBLED) "「困ってる」を投稿します" else "「未来への手紙」を投稿します",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
        }

        // ★ 大きな2択スイッチ
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LabelSelectButton(
                text = PostLabel.TROUBLED.displayName,
                selected = label == PostLabel.TROUBLED,
                color = TroubledAccent,
                modifier = Modifier.weight(1f)
            ) { label = PostLabel.TROUBLED }

            LabelSelectButton(
                text = PostLabel.LETTER_TO_FUTURE.displayName,
                selected = label == PostLabel.LETTER_TO_FUTURE,
                color = LetterAccent,
                modifier = Modifier.weight(1f)
            ) { label = PostLabel.LETTER_TO_FUTURE }
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("タイトル") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = detail, onValueChange = { detail = it },
                label = { Text("補足情報（例: 3階トイレ横）") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = { imagePicker.launch("image/*") }) { Text("写真を選択（任意）") }
            imageUri?.let {
                AsyncImage(
                    model = it, contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .padding(top = 8.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Button(onClick = { showMap = true }) {
                Text(if (pickedLatLng == null) "地図で場所を選ぶ" else "場所選択済み ✓ ${placeName ?: ""}")
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    isPosting = true
                    val geo = pickedLatLng?.let { GeoPoint(it.latitude, it.longitude) }
                    scope.launch {
                        repository.createPost(
                            Post(
                                title = title, label = label.name, locationDetail = detail,
                                geo = geo, placeName = placeName, authorUid = currentUid
                            ),
                            imageUri
                        )
                        isPosting = false
                        onDone()
                    }
                },
                enabled = title.isNotBlank() && pickedLatLng != null && !isPosting,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("投稿する", color = Color.White)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LabelSelectButton(
    text: String,
    selected: Boolean,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clickable(onClick = onClick),
        color = if (selected) color else Color.White,
        contentColor = if (selected) Color.White else color,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(2.dp, color),
        tonalElevation = if (selected) 4.dp else 0.dp
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text, style = MaterialTheme.typography.titleMedium)
        }
    }
}