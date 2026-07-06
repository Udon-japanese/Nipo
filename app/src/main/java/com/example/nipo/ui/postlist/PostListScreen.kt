package com.example.nipo.ui.postlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.ui.common.PostLabelTag
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PostListScreen(
    onCreatePost: () -> Unit,
    onOpenPost: (String) -> Unit
) {
    val repository = remember { PostRepository() }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.observePosts().collectLatest { posts = it }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) { Text("+") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(posts) { post ->
                Card(
                    onClick = { onOpenPost(post.id) },
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        PostLabelTag(post.label)
                        Text(post.title, style = MaterialTheme.typography.titleMedium)
                        Text(post.locationDetail)
                        post.placeName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        post.photoUrl?.let {
                            AsyncImage(
                                model = it, contentDescription = null,
                                modifier = Modifier.fillMaxWidth().height(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}