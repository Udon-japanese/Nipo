package com.example.nipo.data

import android.content.Context
import android.net.Uri
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class PostRepository(private val context: Context) {
    private val db = Firebase.firestore
    private val storage = Firebase.storage

    suspend fun getPost(postId: String): Post? = try {
        db.collection("posts").document(postId).get().await().toObject(Post::class.java)
    } catch (_: Exception) {
        null
    }

    suspend fun createPost(post: Post, imageUris: List<Uri>): Result<Unit> = try {
        val photoUrls = imageUris.map { uploadImage(it, "post_images") }
        val docRef = db.collection("posts").document()
        val data = post.copy(id = docRef.id, photoUrls = photoUrls, createdAt = Timestamp.now())
        docRef.set(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updatePost(postId: String, post: Post, newImageUris: List<Uri>): Result<Unit> = try {
        val photoUrls = if (newImageUris.isEmpty()) post.photoUrls else newImageUris.map { uploadImage(it, "post_images") }
        val docRef = db.collection("posts").document(postId)
        docRef.set(post.copy(id = postId, photoUrls = photoUrls)).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun deletePost(postId: String): Result<Unit> = try {
        db.collection("posts").document(postId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Sets (or clears, when [reaction] is null) the calling user's single reaction on a post.
     * Toggling to the same reaction clears it; switching from one to the other flips both counters
     * in one transaction. [reaction] must be "good", "bad", or null.
     */
    suspend fun setReaction(postId: String, uid: String, reaction: String?): Result<Unit> = try {
        val postRef = db.collection("posts").document(postId)
        val reactionRef = postRef.collection("reactions").document(uid)
        db.runTransaction { tx ->
            val existing = tx.get(reactionRef)
            val previous = if (existing.exists()) existing.getString("type") else null

            var goodDelta = 0L
            var badDelta = 0L
            if (previous == "good") goodDelta -= 1
            if (previous == "bad") badDelta -= 1
            if (reaction == "good") goodDelta += 1
            if (reaction == "bad") badDelta += 1

            if (reaction == null) {
                tx.delete(reactionRef)
            } else {
                tx.set(reactionRef, mapOf("type" to reaction))
            }
            if (goodDelta != 0L) tx.update(postRef, "goodCount", FieldValue.increment(goodDelta))
            if (badDelta != 0L) tx.update(postRef, "badCount", FieldValue.increment(badDelta))
            null
        }.await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getUserReaction(postId: String, uid: String): String? = try {
        db.collection("posts").document(postId).collection("reactions").document(uid)
            .get().await().getString("type")
    } catch (_: Exception) {
        null
    }

    suspend fun addComment(postId: String, comment: Comment, imageUri: Uri?): Result<Unit> = try {
        val photoUrl = imageUri?.let { uploadImage(it, "comment_images") }
        val docRef = db.collection("posts").document(postId).collection("comments").document()
        docRef.set(comment.copy(id = docRef.id, photoUrl = photoUrl, createdAt = Timestamp.now())).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun observePosts() =
        db.collection("posts").orderBy("createdAt", Query.Direction.DESCENDING).snapshots()
            .map { it.toObjects(Post::class.java) }

    fun observeComments(postId: String) =
        db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING).snapshots()
            .map { it.toObjects(Comment::class.java) }

    private suspend fun uploadImage(uri: Uri, folder: String): String =
        ImageUploadUtil.uploadImage(storage, context, uri, folder)
}