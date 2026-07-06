package com.example.nipo.ui.postdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.ui.common.PostLabelTag
import com.example.nipo.data.Comment
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.flow.collectLatest


@Composable
fun PostDetailScreen(postId: String) {
    val repository = remember { PostRepository() }
    var comments by remember { mutableStateOf<List<Comment>>(emptyList()) }
    var text by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val scope = rememberCoroutineScope()
    val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var post by remember { mutableStateOf<Post?>(null) }
    var sendError by remember { mutableStateOf<String?>(null) }


    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> imageUri = uri }

    LaunchedEffect(postId) {
        post = repository.getPost(postId)
    }

    LaunchedEffect(postId) {
        repository.observeComments(postId).collectLatest { comments = it }
    }

    Column(Modifier.fillMaxSize()) {
        post?.let { p ->
            Column(Modifier.padding(16.dp)) {
                PostLabelTag(p.label)
                Text(p.title, style = MaterialTheme.typography.titleLarge)
                p.placeName?.let { Text(it) }
                Text(p.locationDetail)
                p.photoUrl?.let {
                    AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxWidth().height(200.dp))
                }
                post?.geo?.let { geo ->
                    val latLng = LatLng(geo.latitude, geo.longitude)
                    val previewCameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(latLng, 18f)
                    }
                    GoogleMap(
                        modifier = Modifier.fillMaxWidth().height(160.dp).padding(top = 8.dp),
                        cameraPositionState = previewCameraState,
                        uiSettings = MapUiSettings(
                            zoomControlsEnabled = false,
                            scrollGesturesEnabled = false,
                            zoomGesturesEnabled = true,
                            tiltGesturesEnabled = false,
                            rotationGesturesEnabled = false
                        )
                    ) {
                        Marker(state = rememberMarkerState(position = latLng))
                    }
                }
                Divider(Modifier.padding(top = 12.dp))
            }
        }
        LazyColumn(Modifier.weight(1f)) {
            items(comments) { comment ->
                Column(Modifier.padding(8.dp)) {
                    Text("匿名", style = MaterialTheme.typography.labelMedium)
                    Text(comment.text)
                    comment.photoUrl?.let {
                        AsyncImage(
                            model = it, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(120.dp)
                        )
                    }
                    Divider(Modifier.padding(top = 8.dp))
                }
            }
        }
        Row(Modifier.padding(8.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                label = { Text("コメント") }
            )
            Spacer(Modifier.width(4.dp))
            Button(onClick = { imagePicker.launch("image/*") }) { Text("写真") }
            Spacer(Modifier.width(4.dp))
            Button(
                onClick = {
                    scope.launch {
                        val result = repository.addComment(
                            postId,
                            Comment(text = text, authorUid = currentUid),
                            imageUri
                        )
                        result.onSuccess {
                            text = ""
                            imageUri = null
                            sendError = null
                        }.onFailure {
                            sendError = it.message
                        }
                    }
                },
                enabled = text.isNotBlank()
            ) { Text("送信") }

            sendError?.let {
                Text("送信エラー: $it", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(4.dp))
            }
        }
    }
}