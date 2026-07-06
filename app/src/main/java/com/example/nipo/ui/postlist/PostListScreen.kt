package com.example.nipo.ui.postlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.nipo.data.Post
import com.example.nipo.data.PostRepository
import com.example.nipo.ui.common.PostLabelTag
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PostListScreen(
    onCreatePost: () -> Unit,
    onOpenPost: (String) -> Unit
) {
    val context = LocalContext.current
    val repository = remember { PostRepository(context) }
    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }

    LaunchedEffect(Unit) {
        repository.observePosts().collectLatest { posts = it }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreatePost) { Text("+") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            items(posts) { post ->
                Card(
                    onClick = { onOpenPost(post.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        PostLabelTag(post.label)
                        Text(post.title, style = MaterialTheme.typography.titleMedium)
                        Text(post.locationDetail)
                        post.placeName?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                        post.photoUrl?.let {
                            AsyncImage(
                                model = it, contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}