package com.example.nipo.data

import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostRepository {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    suspend fun getPost(postId: String): Post? = try {
        db.collection("posts").document(postId).get().await().toObject(Post::class.java)
    } catch (e: Exception) { null }

    suspend fun createPost(post: Post, imageUri: Uri?): Result<Unit> = try {
        val photoUrl = imageUri?.let { uploadImage(it, "post_images") }
        val docRef = db.collection("posts").document()
        val data = post.copy(id = docRef.id, photoUrl = photoUrl)
        docRef.set(data).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    suspend fun addComment(postId: String, comment: Comment, imageUri: Uri?): Result<Unit> = try {
        val photoUrl = imageUri?.let { uploadImage(it, "comment_images") }
        val docRef = db.collection("posts").document(postId)
            .collection("comments").document()
        docRef.set(comment.copy(id = docRef.id, photoUrl = photoUrl)).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    fun observePosts() = db.collection("posts")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .snapshots()
        .map { it.toObjects(Post::class.java) }

    fun observeComments(postId: String) = db.collection("posts").document(postId)
        .collection("comments")
        .orderBy("createdAt", Query.Direction.ASCENDING)
        .snapshots()
        .map { it.toObjects(Comment::class.java) }

    private suspend fun uploadImage(uri: Uri, folder: String): String {
        val ref = storage.reference.child("$folder/${UUID.randomUUID()}.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }
}